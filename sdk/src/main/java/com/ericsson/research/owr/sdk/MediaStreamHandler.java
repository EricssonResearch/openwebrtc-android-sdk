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


import android.util.Log;

import com.ericsson.research.owr.MediaSession;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Payload;
import com.ericsson.research.owr.RemoteMediaSource;
import com.ericsson.research.owr.Session;

import java.util.List;

class MediaStreamHandler extends StreamHandler implements MediaSession.OnIncomingSourceListener, MediaSession.CnameChangeListener, StreamSet.MediaSourceDelegate, MediaSession.SendSsrcChangeListener {
    private static final String TAG = "MediaStreamHandler";
    private boolean mShouldRespectRemotePayloadOrder;
    private List<RtcPayload> mDefaultPayloads;

    private boolean mHaveCname = false;
    private boolean mHaveSsrc = false;

    MediaStreamHandler(int index, StreamDescription streamDescription, StreamSet.MediaStream mediaStream, RtcConfig config) {
        super(index, streamDescription, mediaStream);
        if (mediaStream == null) {
            return;
        }
        getMediaSession().addCnameChangeListener(this);
        getMediaSession().addSendSsrcChangeListener(this);
        getMediaSession().addOnIncomingSourceListener(this);
        getMediaStream().setMediaSourceDelegate(this);

        boolean haveRemoteDescription = getRemoteStreamDescription() != null;

        String mediaStreamId = getMediaStream().getId();
        if (mediaStreamId == null) {
            getLocalStreamDescription().setMediaStreamId(Utils.randomString(27));
        } else {
            getLocalStreamDescription().setMediaStreamId(mediaStreamId);
        }
        getLocalStreamDescription().setMediaStreamTrackId(Utils.randomString(27));

        boolean rtcpMux;
        StreamMode mode;
        boolean wantSend = getMediaStream().wantSend();
        boolean wantReceive = getMediaStream().wantReceive();

        if (!haveRemoteDescription) {
            mode = StreamMode.get(wantSend, wantReceive);
            rtcpMux = true;
        } else {
            mode = getRemoteStreamDescription().getMode().reverse(wantSend, wantReceive);
            rtcpMux = getRemoteStreamDescription().isRtcpMux();
            getStream().setStreamMode(mode);
        }

        getLocalStreamDescription().setMode(mode);

        if (mode == StreamMode.INACTIVE) {
            getStream().setStreamMode(mode);
            return;
        }

        getMediaSession().setRtcpMux(rtcpMux);
        getLocalStreamDescription().setRtcpMux(rtcpMux);

        if (getMediaStream().getMediaType() == MediaType.VIDEO) {
            mDefaultPayloads = config.getDefaultVideoPayloads();
        } else {
            mDefaultPayloads = config.getDefaultAudioPayloads();
        }
        mShouldRespectRemotePayloadOrder = config.shouldRespectRemotePayloadOrder();
        List<RtcPayload> payloads = mDefaultPayloads;
        if (haveRemoteDescription) {
            payloads = Utils.intersectPayloads(getRemoteStreamDescription().getPayloads(), payloads);
            if (!mShouldRespectRemotePayloadOrder) {
                payloads = Utils.reorderPayloadsByFilter(payloads, mDefaultPayloads);
            }
        }
        for (RtcPayload payload : payloads) {
            getLocalStreamDescription().addPayload(payload);
        }
        List<Payload> transformedPayloads = Utils.transformPayloads(payloads, getMediaStream().getMediaType());
        if (transformedPayloads.isEmpty()) {
            Log.w(TAG, "no suitable payload found for stream: " + getMediaStream().getId());
            getStream().setStreamMode(StreamMode.INACTIVE);
            // TODO: stop stream
            return;
        }

        if (mode.wantReceive()) {
            for (Payload payload : transformedPayloads) {
                getMediaSession().addReceivePayload(payload);
            }
        }
        if (haveRemoteDescription && mode.wantSend()) {
            getMediaSession().setSendPayload(transformedPayloads.get(0));
        }
    }

    public MediaSession getMediaSession() {
        return (MediaSession) getSession();
    }

    public StreamSet.MediaStream getMediaStream() {
        return (StreamSet.MediaStream) getStream();
    }

    @Override
    Session createSession(final boolean isDtlsClient) {
        return new MediaSession(isDtlsClient);
    }

    @Override
    public void setRemoteStreamDescription(StreamDescription streamDescription) {
        super.setRemoteStreamDescription(streamDescription);
        StreamMode mode = getRemoteStreamDescription().getMode().reverse(
                getMediaStream().wantSend(), getMediaStream().wantReceive()
        );
        getLocalStreamDescription().setMode(mode);
        getStream().setStreamMode(mode);
        if (mode == StreamMode.INACTIVE) {
            return;
        }
        if (!getRemoteStreamDescription().isRtcpMux()) {
            getMediaSession().setRtcpMux(false);
        }

        if (mode.wantSend()) {
            List<RtcPayload> payloads =  getRemoteStreamDescription().getPayloads();
            if (!mShouldRespectRemotePayloadOrder) {
                payloads = Utils.reorderPayloadsByFilter(payloads, mDefaultPayloads);
            }
            List<Payload> transformedPayloads = Utils.transformPayloads(payloads, getMediaStream().getMediaType());
            if (transformedPayloads.isEmpty()) {
                Log.w(TAG, "no suitable payload found for stream: " + getMediaStream().getId());
                getStream().setStreamMode(StreamMode.INACTIVE);
                // TODO: stop stream
                return;
            }
            getMediaSession().setSendPayload(transformedPayloads.get(0));
        }
    }

    @Override
    public boolean isReady() {
        boolean isInactive = getLocalStreamDescription().getMode() == StreamMode.INACTIVE;
        return super.isReady() && mHaveSsrc && mHaveCname || isInactive;
    }

    @Override
    public void stop() {
        if (getMediaSession() != null) {
            getMediaSession().removeCnameChangeListener(this);
            getMediaSession().removeSendSsrcChangeListener(this);
            getMediaSession().removeOnIncomingSourceListener(this);
            getMediaSession().setSendSource(null);
        }
        if (getMediaStream() != null) {
            getMediaStream().onRemoteMediaSource(null);
            getMediaStream().setMediaSourceDelegate(null);
        }
        super.stop();
    }

    @Override
    public void onCnameChanged(String cname) {
        getLocalStreamDescription().setCname(cname);
        mHaveCname = true;

        signalListenerIfReady();
    }

    @Override
    public void onSendSsrcChanged(final int ssrc) {
        long unsignedSsrc = ssrc & 0xFFFFFFFFL;
        if (unsignedSsrc > 0) {
            getLocalStreamDescription().addSsrc(unsignedSsrc);
            mHaveSsrc = true;

            signalListenerIfReady();
        }
    }

    @Override
    public void onIncomingSource(RemoteMediaSource remoteMediaSource) {
        if (getSession() != null) {
            getMediaStream().onRemoteMediaSource(remoteMediaSource);
        }
    }

    @Override
    public void setMediaSource(MediaSource mediaSource) {
        if (getSession() != null) {
            getMediaSession().setSendSource(mediaSource);
        }
    }
}