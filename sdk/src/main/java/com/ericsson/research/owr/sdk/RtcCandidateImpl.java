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

import com.ericsson.research.owr.Candidate;

class RtcCandidateImpl implements RtcCandidate {
    public static String TAG = "PlainRtcCandidate";

    private int mStreamIndex;
    private String mStreamId;
    private String mUfrag;
    private String mPassword;
    private final String mFoundation;
    private final ComponentType mComponentType;
    private final TransportType mTransportType;
    private final int mPriority;
    private final String mAddress;
    private final int mPort;
    private final CandidateType mType;
    private final String mRelatedAddress;
    private final int mRelatedPort;

    RtcCandidateImpl(int streamIndex, String streamId, String ufrag, String password, String foundation, ComponentType componentType, TransportType transportType, int priority, String address, int port, CandidateType type, String relatedAddress, int relatedPort) {
        mStreamIndex = streamIndex;
        mStreamId = streamId;
        mUfrag = ufrag;
        mPassword = password;
        mFoundation = foundation;
        mComponentType = componentType;
        mTransportType = transportType;
        mPriority = priority;
        mAddress = address;
        mPort = port;
        mType = type;
        mRelatedAddress = relatedAddress;
        mRelatedPort = relatedPort;
    }

    RtcCandidateImpl(String foundation, ComponentType componentType, TransportType transportType, int priority, String address, int port, CandidateType type, String relatedAddress, int relatedPort) {
        this(-1, null, null, null, foundation, componentType, transportType, priority, address, port, type, relatedAddress, relatedPort);
    }

    public void setCredentials(String ufrag, String password) {
        mUfrag = ufrag;
        mPassword = password;
    }

    public void setStreamIndex(final int streamIndex) {
        mStreamIndex = streamIndex;
    }

    public void setStreamId(final String streamLabel) {
        mStreamId = streamLabel;
    }

    @Override
    public int getStreamIndex() {
        return mStreamIndex;
    }

    @Override
    public String getStreamId() {
        return mStreamId;
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
    public String getFoundation() {
        return mFoundation;
    }

    @Override
    public ComponentType getComponentType() {
        return mComponentType;
    }

    @Override
    public TransportType getTransportType() {
        return mTransportType;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public int getPort() {
        return mPort;
    }

    @Override
    public CandidateType getType() {
        return mType;
    }

    @Override
    public String getRelatedAddress() {
        return mRelatedAddress;
    }

    @Override
    public int getRelatedPort() {
        return mRelatedPort;
    }

    static RtcCandidateImpl fromOwrCandidate(Candidate candidate) {
        return new RtcCandidateImpl(
                -1, null,
                candidate.getUfrag(),
                candidate.getPassword(),
                candidate.getFoundation(),
                ComponentType.valueOf(candidate.getComponentType().name()),
                TransportType.valueOf(candidate.getTransportType().name()),
                candidate.getPriority(),
                candidate.getAddress(),
                candidate.getPort(),
                CandidateType.valueOf(candidate.getType().name()),
                candidate.getBaseAddress(),
                candidate.getBasePort()
        );
    }
}
