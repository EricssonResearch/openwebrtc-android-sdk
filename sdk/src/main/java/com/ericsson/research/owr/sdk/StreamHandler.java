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

import com.ericsson.research.owr.Candidate;
import com.ericsson.research.owr.Session;

import java.lang.ref.WeakReference;

// TODO: verify peer cert
abstract class StreamHandler implements Session.DtlsCertificateChangeListener, Session.OnNewCandidateListener {
    private static final String TAG = "StreamHandler";

    private static final String DEFAULT_HASH_FUNCTION = "sha-256";

    private final StreamSet.Stream mStream;
    private final MutableStreamDescription mLocalStreamDescription;
    private final int mIndex;
    private StreamDescription mRemoteStreamDescription;
    private Session mSession;
    private boolean mHaveCandidate = false;
    private boolean mHaveFingerprint = false;
    private boolean mLocalDescriptionCreated = false;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private WeakReference<RtcSessionDelegate> mRtcSessionDelegateRef = new WeakReference<>(null);

    StreamHandler(int index, StreamDescription streamDescription, StreamSet.Stream stream) {
        mLocalStreamDescription = new MutableStreamDescription();
        mRemoteStreamDescription = streamDescription;
        mIndex = index;
        mStream = stream;

        if (stream == null) { // Inactive stream
            mLocalStreamDescription.setMode(StreamMode.INACTIVE);
            mLocalStreamDescription.setType(streamDescription.getType());
            return;
        }

        mSession = createSession(streamDescription != null);

        if (streamDescription != null) {
            for (RtcCandidate rtcCandidate : getRemoteStreamDescription().getCandidates()) {
                Candidate candidate = Utils.transformCandidate(rtcCandidate);
                candidate.setUfrag(streamDescription.getUfrag());
                candidate.setPassword(streamDescription.getPassword());
                mSession.addRemoteCandidate(candidate);
            }
        }

        mSession.addDtlsCertificateChangeListener(this);
        mSession.addOnNewCandidateListener(this);

        mLocalStreamDescription.setType(getStream().getType());

        String fingerprintHashFunction;
        String dtlsSetup;

        if (streamDescription == null) {
            fingerprintHashFunction = DEFAULT_HASH_FUNCTION;
            dtlsSetup = "actpass";
        } else {
            fingerprintHashFunction = getRemoteStreamDescription().getFingerprintHashFunction();
            dtlsSetup = "active";
        }

        getLocalStreamDescription().setFingerprintHashFunction(fingerprintHashFunction);
        getLocalStreamDescription().setDtlsSetup(dtlsSetup);
    }

    abstract Session createSession(boolean isDtlsClient);

    public boolean haveRemoteDescription() {
        return mRemoteStreamDescription != null;
    }

    public int getIndex() {
        return mIndex;
    }

    public Session getSession() {
        return mSession;
    }

    public StreamSet.Stream getStream() {
        return mStream;
    }

    public MutableStreamDescription getLocalStreamDescription() {
        return mLocalStreamDescription;
    }

    public StreamDescription getRemoteStreamDescription() {
        return mRemoteStreamDescription;
    }

    public StreamDescription finishLocalStreamDescription() {
        mLocalDescriptionCreated = true;
        return getLocalStreamDescription();
    }

    public boolean isReady() {
        boolean isInactive = getLocalStreamDescription().getMode() == StreamMode.INACTIVE;
        return mHaveCandidate && mHaveFingerprint || isInactive;
    }

    public void setRemoteStreamDescription(StreamDescription remoteStreamDescription) {
        mRemoteStreamDescription = remoteStreamDescription;

        for (RtcCandidate rtcCandidate : getRemoteStreamDescription().getCandidates()) {
            Candidate candidate = Utils.transformCandidate(rtcCandidate);
            candidate.setUfrag(remoteStreamDescription.getUfrag());
            candidate.setPassword(remoteStreamDescription.getPassword());
            getSession().addRemoteCandidate(candidate);
        }
    }

    public void stop() {
        if (getSession() != null) {
            getSession().removeDtlsCertificateChangeListener(this);
            getSession().removeOnNewCandidateListener(this);
        }
        mSession = null;
        mRemoteStreamDescription = null;
    }

    public void signalListenerIfReady() {
        RtcSessionDelegate delegate = mRtcSessionDelegateRef.get();
        if (delegate != null && isReady()) {
            delegate.onReady();
        }
    }

    @Override
    public synchronized void onDtlsCertificateChanged(String pem) {
        String fingerprintHashFunction = getLocalStreamDescription().getFingerprintHashFunction();
        String fingerprint = Utils.fingerprintFromPem(pem, fingerprintHashFunction);
        getLocalStreamDescription().setFingerprint(fingerprint);
        mHaveFingerprint = true;
        signalListenerIfReady();
    }

    @Override
    public synchronized void onNewCandidate(Candidate candidate) {
        if (!mHaveCandidate) {
            getLocalStreamDescription().setUfrag(candidate.getUfrag());
            getLocalStreamDescription().setPassword(candidate.getPassword());
        }

        final RtcCandidateImpl rtcCandidate = RtcCandidateImpl.fromOwrCandidate(candidate);
        if (mLocalDescriptionCreated) {
            Log.d(TAG, "[RtcSession] got local candidate for " + this);
            rtcCandidate.setStreamIndex(getIndex());
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    RtcSessionDelegate delegate = mRtcSessionDelegateRef.get();
                    if (delegate != null) {
                        delegate.onLocalCandidate(rtcCandidate);
                    }
                }
            });
        } else {
            getLocalStreamDescription().addCandidate(rtcCandidate);
        }


        if (!mHaveCandidate) {
            mHaveCandidate = true;
            signalListenerIfReady();
        }
    }

    public void onRemoteCandidate(RtcCandidate rtcCandidate) {
        if (getStream() == null) {
            return;
        }
        boolean isRtcp = rtcCandidate.getComponentType() == RtcCandidate.ComponentType.RTCP;
        if (getLocalStreamDescription().isRtcpMux() && isRtcp) {
            return;
        }
        Candidate candidate = Utils.transformCandidate(rtcCandidate);
        candidate.setUfrag(getRemoteStreamDescription().getUfrag());
        candidate.setPassword(getRemoteStreamDescription().getPassword());
        getSession().addRemoteCandidate(candidate);
    }

    public void setRtcSessionDelegate(RtcSessionDelegate delegate) {
        mRtcSessionDelegateRef = new WeakReference<RtcSessionDelegate>(delegate);
    }

    public interface RtcSessionDelegate {
        void onReady();

        void onLocalCandidate(RtcCandidate candidate);
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    @Override
    public String toString() {
        if (getLocalStreamDescription() == null) {
            return "Stream{}";
        }
        return "Stream{" +
                getLocalStreamDescription().getType().toString().charAt(0) + getIndex() + "," +
                getLocalStreamDescription().getMode() + "}";
    }
}
