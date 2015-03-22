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

import java.util.List;

class StreamDescriptionImpl implements StreamDescription {
    public static final String TAG = "StreamDescriptionImpl";

    private final String mId;
    private final StreamType mStreamType;
    private final StreamMode mMode;
    private final String mUfrag;
    private final String mPassword;
    private final List<RtcCandidate> mCandidates;
    private final String mDtlsSetup;
    private final String mFingerprint;
    private final String mFingerprintHashFunction;
    // media only
    private final String mMediaStreamId;
    private final String mMediaStreamTrackId;
    private final String mCname;
    private final boolean mRtcpMux;
    private final List<Long> mSsrcs;
    private final List<RtcPayload> mPayloads;
    // data only
    private final int mSctpPort;
    private final int mMaxMessageSize;
    private final String mAppLabel;

    private StreamDescriptionImpl(String id, StreamType streamType, StreamMode mode, String ufrag, String password, List<RtcCandidate> candidates, String dtlsSetup, String fingerprint, String fingerprintHashFunction, String mediaStreamId, String mediaStreamTrackId, String cname, boolean rtcpMux, List<Long> ssrcs, List<RtcPayload> payloads, int sctpPort, int maxMessageSize, String appLabel) {
        mId = id;
        mStreamType = streamType;
        mMode = mode;
        mUfrag = ufrag;
        mPassword = password;
        mCandidates = candidates;
        mDtlsSetup = dtlsSetup;
        mFingerprint = fingerprint;
        mFingerprintHashFunction = fingerprintHashFunction;
        mMediaStreamId = mediaStreamId;
        mMediaStreamTrackId = mediaStreamTrackId;
        mCname = cname;
        mRtcpMux = rtcpMux;
        mSsrcs = ssrcs;
        mPayloads = payloads;
        mSctpPort = sctpPort;
        mMaxMessageSize = maxMessageSize;
        mAppLabel = appLabel;
    }

    StreamDescriptionImpl(String id, StreamType streamType, StreamMode mode, String ufrag, String password, List<RtcCandidate> candidates, String dtlsSetup, String fingerprint, String fingerprintHashFunction, String mediaStreamId, String mediaStreamTrackId, String cname, boolean rtcpMux, List<Long> ssrcs, List<RtcPayload> payloads) {
        this(id, streamType, mode, ufrag, password, candidates, dtlsSetup, fingerprint, fingerprintHashFunction, mediaStreamId, mediaStreamTrackId, cname, rtcpMux, ssrcs, payloads, -1, -1, null);
    }

    StreamDescriptionImpl(String id, StreamType streamType, StreamMode mode, String ufrag, String password, List<RtcCandidate> candidates, String dtlsSetup, String fingerprint, String fingerprintHashFunction, int sctpPort, int maxMessageSize, String appLabel) {
        this(id, streamType, mode, ufrag, password, candidates, dtlsSetup, fingerprint, fingerprintHashFunction, null, null, null, false, null, null, sctpPort, maxMessageSize, appLabel);
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public StreamType getStreamType() {
        return mStreamType;
    }

    @Override
    public StreamMode getMode() {
        return mMode;
    }

    @Override
    public String getUfrag() {
        return mUfrag;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }

    @Override
    public int getCandidateCount() {
        return mCandidates == null ? 0 : mCandidates.size();
    }

    @Override
    public RtcCandidate getCandidate(final int index) {
        return mCandidates.get(index);
    }

    @Override
    public String getDtlsSetup() {
        return mDtlsSetup;
    }

    @Override
    public String getFingerprint() {
        return mFingerprint;
    }

    @Override
    public String getFingerprintHashFunction() {
        return mFingerprintHashFunction;
    }

    @Override
    public String getMediaStreamId() {
        return mMediaStreamId;
    }

    @Override
    public String getMediaStreamTrackId() {
        return mMediaStreamTrackId;
    }

    @Override
    public String getCname() {
        return mCname;
    }

    public boolean isRtcpMux() {
        return mRtcpMux;
    }

    @Override
    public int getSsrcCount() {
        return mSsrcs == null ? 0 : mSsrcs.size();
    }

    @Override
    public long getSsrc(final int index) {
        return mSsrcs.get(index);
    }

    @Override
    public int getPayloadCount() {
        return mPayloads == null ? 0 : mPayloads.size();
    }

    @Override
    public RtcPayload getPayload(int index) {
        return mPayloads.get(index);
    }

    @Override
    public int getSctpPort() {
        return mSctpPort;
    }

    @Override
    public int getMaxMessageSize() {
        return mMaxMessageSize;
    }

    @Override
    public String getAppLabel() {
        return mAppLabel;
    }
}
