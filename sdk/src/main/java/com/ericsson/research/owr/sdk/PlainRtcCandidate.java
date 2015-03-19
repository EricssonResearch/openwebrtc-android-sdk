/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import com.ericsson.research.owr.CandidateType;
import com.ericsson.research.owr.ComponentType;
import com.ericsson.research.owr.TransportType;

class PlainRtcCandidate implements RtcCandidate {
    public static String TAG = "PlainRtcCandidate";

    public int mStreamIndex;
    public String mStreamId;
    public String mUfrag;
    public String mPassword;
    public String mFoundation;
    public ComponentType mComponentType;
    public TransportType mTransportType;
    public int mPriority;
    public String mAddress;
    public int mPort;
    public CandidateType mType;
    public String mRelatedAddress;
    public int mRelatedPort;

    PlainRtcCandidate(int streamIndex, String streamId, String ufrag, String password, String foundation, ComponentType componentType, TransportType transportType, int priority, String address, int port, CandidateType type, String relatedAddress, int relatedPort) {
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

    PlainRtcCandidate(String foundation, ComponentType componentType, TransportType transportType, int priority, String address, int port, CandidateType type, String relatedAddress, int relatedPort) {
        this(-1, null, null, null, foundation, componentType, transportType, priority, address, port, type, relatedAddress, relatedPort);
    }

    PlainRtcCandidate(String foundation, ComponentType componentType, TransportType transportType, int priority, String address, int port, CandidateType type) {
        this(-1, null, null, null, foundation, componentType, transportType, priority, address, port, type, null, -1);
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
}
