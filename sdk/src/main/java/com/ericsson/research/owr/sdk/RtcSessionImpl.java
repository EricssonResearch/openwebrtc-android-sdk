/*
 * Copyright (c) 2015, Ericsson AB.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.ericsson.research.owr.sdk;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import com.ericsson.research.owr.Candidate;
import com.ericsson.research.owr.DataChannel;
import com.ericsson.research.owr.DataSession;
import com.ericsson.research.owr.MediaSession;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Payload;
import com.ericsson.research.owr.RemoteMediaSource;
import com.ericsson.research.owr.Session;
import com.ericsson.research.owr.TransportAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class RtcSessionImpl implements RtcSession, StreamHandler.RtcSessionDelegate {
    private static final String TAG = "RtcSessionImpl";

    private enum State {
        INIT, RECEIVED_OFFER, SETUP, AWAITING_ANSWER, ACTIVE, STOPPED
    }

    private TransportAgent mTransportAgent;

    private boolean mIsInitiator;
    private final String mSessionId;
    private final RtcConfig mConfig;

    private SessionDescription mRemoteDescription = null;
    private final Handler mMainHandler;

    private OnLocalCandidateListener mLocalCandidateListener = null;
    private List<StreamHandler> mStreamHandlers;
    private OnLocalDescriptionListener mLocalDescriptionListener;
    private List<RtcCandidate> mRemoteCandidateBuffer;
    private State mState;

    private static Random sRandom = new Random();

    RtcSessionImpl(RtcConfig config) {
        mSessionId = "" + (sRandom.nextInt() + new Date().getTime());
        mConfig = config;
        mState = State.INIT;
        mIsInitiator = true;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public synchronized void setOnLocalCandidateListener(final OnLocalCandidateListener listener) {
        mLocalCandidateListener = listener;
    }

    @Override
    public void setOnLocalDescriptionListener(final OnLocalDescriptionListener listener) {
        mLocalDescriptionListener = listener;
    }

    @Override
    public void onReady() {
        maybeFinishSetup();
    }

    @Override
    public void onLocalCandidate(final RtcCandidate candidate) {
        if (mLocalCandidateListener != null) {
            mLocalCandidateListener.onLocalCandidate(candidate);
        }
    }

    private void log(String msg) {
        String streams;
        if (mStreamHandlers == null) {
            streams = "[]";
        } else {
            streams = Arrays.toString(mStreamHandlers.toArray(new StreamHandler[mStreamHandlers.size()]));
        }
        Log.d(TAG, "[RtcSession" +
                " id=" + mSessionId +
                " initiator=" + isInitiator() +
                " state=" + mState.name() +
                " streams=" + streams +
                " candidates=" + (mRemoteCandidateBuffer == null ? 0 : mRemoteCandidateBuffer.size()) +
                " ] " + msg);
    }

    @Override
    public synchronized void start(final StreamSet streamSet) {
        if (mState != State.INIT && mState != State.RECEIVED_OFFER) {
            throw new IllegalStateException("setup called at wrong state: " + mState.name());
        }
        if (streamSet == null) {
            throw new NullPointerException("streamSet may not be null");
        }
        log("setup called");

        mTransportAgent = new TransportAgent(mIsInitiator);

        for (RtcConfig.HelperServer helperServer : mConfig.getHelperServers()) {
            mTransportAgent.addHelperServer(
                    helperServer.getType(),
                    helperServer.getAddress(),
                    helperServer.getPort(),
                    helperServer.getUsername(),
                    helperServer.getPassword()
            );
        }

        mStreamHandlers = new LinkedList<>();
        int index = 0;
        if (mIsInitiator) {
            // For outbound calls we initiate all streams without any remote description
            for (StreamSet.Stream stream : streamSet.getStreams()) {
                mStreamHandlers.add(createStreamHandler(index, null, stream));
                index++;
            }
        } else {
            for (Pair<StreamDescription, StreamSet.Stream> pair : Utils.resolveOfferedStreams(mRemoteDescription, streamSet.getStreams())) {
                StreamSet.Stream stream = pair.second;
                if (stream == null) {
                    stream = streamSet.handleUnusedStream(pair.first);
                }
                mStreamHandlers.add(createStreamHandler(index, pair.first, stream));
                index++;
            }
        }
        for (StreamHandler handler : mStreamHandlers) {
            if (handler.getSession() != null) {
                mTransportAgent.addSession(handler.getSession());
            }
        }

        mState = State.SETUP;

        if (mRemoteDescription != null && mRemoteCandidateBuffer != null) {
            for (RtcCandidate candidate : mRemoteCandidateBuffer) {
                addRemoteCandidate(candidate);
            }
            mRemoteCandidateBuffer = null;
        }
        log("initial setup complete");

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                // we might be ready straight away if there are no active streams.
                // do this check asynchronously to keep code paths consistent
                maybeFinishSetup();
            }
        });
    }

    private synchronized void maybeFinishSetup() {
        final SessionDescription sessionDescription;

        if (mState != State.SETUP) {
            Log.w(TAG, "maybeFinishSetup called at wrong state: " + mState);
            return;
        }
        for (StreamHandler streamHandler : mStreamHandlers) {
            if (!streamHandler.isReady()) {
                return;
            }
        }
        log("setup complete");

        List<StreamDescription> streamDescriptions = new ArrayList<>(mStreamHandlers.size());

        for (StreamHandler streamHandler : mStreamHandlers) {
            streamDescriptions.add(streamHandler.finishLocalStreamDescription());
        }

        SessionDescription.Type type;
        if (mIsInitiator) {
            type = SessionDescription.Type.OFFER;
            mState = State.AWAITING_ANSWER;
        } else {
            type = SessionDescription.Type.ANSWER;
            mState = State.ACTIVE;
        }

        sessionDescription = new SessionDescriptionImpl(type, mSessionId, streamDescriptions);

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mLocalDescriptionListener != null) {
                    mLocalDescriptionListener.onLocalDescription(sessionDescription);
                }
            }
        });
    }

    @Override
    public synchronized void setRemoteDescription(final SessionDescription remoteDescription) throws InvalidDescriptionException {
        if (mState != State.AWAITING_ANSWER && mState != State.INIT) {
            throw new IllegalStateException("setRemoteDescription called at wrong state: " + mState);
        }
        if (mRemoteDescription != null) {
            throw new IllegalStateException("remote description has already been set");
        }
        if (remoteDescription == null) {
            throw new NullPointerException("remote description should not be null");
        }
        if (mState == State.INIT) {
            mRemoteDescription = remoteDescription;
            mIsInitiator = false;
            mState = State.RECEIVED_OFFER;
            log("received offer");
            return;
        }
        mRemoteDescription = remoteDescription;
        log("received answer");

        List<StreamDescription> streamDescriptions = remoteDescription.getStreamDescriptions();
        int numStreamDescriptions = streamDescriptions.size();
        int numStreamHandlers = mStreamHandlers.size();

        if (numStreamDescriptions != numStreamHandlers) {
            throw new InvalidDescriptionException("session description has an invalid number of stream descriptions: " +
                    numStreamDescriptions + " != " + numStreamHandlers);
        }
        int size = Math.max(streamDescriptions.size(), mStreamHandlers.size());
        for (int i = 0; i < size; i++) {
            StreamDescription streamDescription = streamDescriptions.get(i);
            StreamHandler streamHandler = mStreamHandlers.get(i);
            if (streamDescription.getType() != streamHandler.getStream().getType()) {
                throw new InvalidDescriptionException("stream description types do not match: " +
                        streamDescription.getType() + " != " + streamHandler.getStream().getType());
            }
            streamHandler.provideAnswer(streamDescription);
        }

        if (mRemoteCandidateBuffer != null) {
            for (RtcCandidate candidate : mRemoteCandidateBuffer) {
                addRemoteCandidate(candidate);
            }
            mRemoteCandidateBuffer = null;
        }
    }

    @Override
    public synchronized void addRemoteCandidate(final RtcCandidate candidate) {
        if (mState == State.STOPPED) {
            return;
        } else if (mRemoteDescription == null || mState == State.RECEIVED_OFFER) {
            if (mRemoteCandidateBuffer == null) {
                mRemoteCandidateBuffer = new LinkedList<>();
            }
            mRemoteCandidateBuffer.add(candidate);
            Log.d(TAG, "[RtcSession] buffering candidate for stream " + candidate.getStreamIndex());
            return;
        }

        StreamHandler streamHandler = null;
        int index = candidate.getStreamIndex();
//        String id = candidate.getStreamId();

        if (index < 0) {
            // TODO: use id?
/*            if (id != null) {
                for (StreamHandler handler : mStreamHandlers) {
                    if (id.equals(handler.getStream().getId())) {
                        streamHandler = handler;
                        break;
                    }
                }
            }*/
        } else if (index < mStreamHandlers.size()) {
            streamHandler = mStreamHandlers.get(index);
        }

        if (streamHandler != null) {
            Log.d(TAG, "[RtcSession] got remote candidate for " + streamHandler);
            streamHandler.onRemoteCandidate(candidate);
        }
    }

    @Override
    public void stop() {
        mState = State.STOPPED;

        if (mStreamHandlers != null) {
            for (StreamHandler streamHandler : mStreamHandlers) {
                streamHandler.stop();
            }
        }

        mRemoteCandidateBuffer = null;
        mRemoteDescription = null;
        mTransportAgent = null;
        mStreamHandlers = null;
    }

    @Override
    public String dumpPipelineGraph() {
        if (mTransportAgent != null) {
            return mTransportAgent.getDotData();
        }
        return null;
    }

    public boolean isInitiator() {
        return mIsInitiator;
    }

    private StreamHandler createStreamHandler(int index, StreamDescription streamDescription, StreamSet.Stream stream) {
        StreamHandler streamHandler;
        if (stream == null) {
            if (streamDescription.getType() == StreamType.DATA) {
                streamHandler = new DataStreamHandler(index, isInitiator(), streamDescription);
            } else {
                streamHandler = new MediaStreamHandler(index, isInitiator(), streamDescription);
            }
        } else if (stream.getType() == StreamType.DATA) {
            streamHandler = new DataStreamHandler(index, isInitiator(), streamDescription, (StreamSet.DataStream) stream);
        } else {
            streamHandler = new MediaStreamHandler(index, isInitiator(), streamDescription, (StreamSet.MediaStream) stream, mConfig);
        }
        streamHandler.setRtcSessionDelegate(this);
        return streamHandler;
    }
}
