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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MutableStreamDescription implements StreamDescription {
    private StreamType mType;
    private StreamMode mMode;
    private String mUfrag;
    private String mPassword;
    private List<RtcCandidate> mCandidates = new LinkedList<>();
    private String mDtlsSetup;
    private String mFingerprint;
    private String mFingerprintHashFunction;
    private String mMediaStreamId;
    private String mMediaStreamTrackId;
    private String mCname;
    private boolean mRtcpMux;
    private List<Long> mSsrcs = new LinkedList<>();
    private List<RtcPayload> mPayloads = new LinkedList<>();
    private int mSctpPort;
    private int mSctpStreamCount;
    private String mAppLabel;

    @Override
    public StreamType getType() {
        return mType;
    }

    public void setType(final StreamType type) {
        mType = type;
    }

    @Override
    public StreamMode getMode() {
        return mMode;
    }

    public void setMode(final StreamMode mode) {
        mMode = mode;
    }

    @Override
    public String getUfrag() {
        return mUfrag;
    }

    public void setUfrag(final String ufrag) {
        mUfrag = ufrag;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(final String password) {
        mPassword = password;
    }

    @Override
    public List<RtcCandidate> getCandidates() {
        return Collections.unmodifiableList(mCandidates);
    }

    public void addCandidate(RtcCandidate candidate) {
        mCandidates.add(candidate);
    }

    @Override
    public String getDtlsSetup() {
        return mDtlsSetup;
    }

    public void setDtlsSetup(final String dtlsSetup) {
        mDtlsSetup = dtlsSetup;
    }

    @Override
    public String getFingerprint() {
        return mFingerprint;
    }

    public void setFingerprint(final String fingerprint) {
        mFingerprint = fingerprint;
    }

    @Override
    public String getFingerprintHashFunction() {
        return mFingerprintHashFunction;
    }

    public void setFingerprintHashFunction(final String fingerprintHashFunction) {
        mFingerprintHashFunction = fingerprintHashFunction;
    }

    @Override
    public String getMediaStreamId() {
        return mMediaStreamId;
    }

    public void setMediaStreamId(final String mediaStreamId) {
        mMediaStreamId = mediaStreamId;
    }

    @Override
    public String getMediaStreamTrackId() {
        return mMediaStreamTrackId;
    }

    public void setMediaStreamTrackId(final String mediaStreamTrackId) {
        mMediaStreamTrackId = mediaStreamTrackId;
    }

    @Override
    public String getCname() {
        return mCname;
    }

    public void setCname(final String cname) {
        mCname = cname;
    }

    @Override
    public boolean isRtcpMux() {
        return mRtcpMux;
    }

    public void setRtcpMux(final boolean rtcpMux) {
        mRtcpMux = rtcpMux;
    }

    @Override
    public List<Long> getSsrcs() {
        return Collections.unmodifiableList(mSsrcs);
    }

    public void addSsrc(final long ssrc) {
        mSsrcs.add(ssrc);
    }

    @Override
    public List<RtcPayload> getPayloads() {
        return Collections.unmodifiableList(mPayloads);
    }

    public void addPayload(RtcPayload payload) {
        mPayloads.add(payload);
    }

    @Override
    public int getSctpPort() {
        return mSctpPort;
    }

    public void setSctpPort(final int sctpPort) {
        mSctpPort = sctpPort;
    }

    @Override
    public int getSctpStreamCount() {
        return mSctpStreamCount;
    }

    public void setSctpStreamCount(final int sctpStreamCount) {
        mSctpStreamCount = sctpStreamCount;
    }

    @Override
    public String getAppLabel() {
        return mAppLabel;
    }

    public void setAppLabel(final String appLabel) {
        mAppLabel = appLabel;
    }
}