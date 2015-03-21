/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

public interface RtcCandidate {
    public int getStreamIndex();

    public String getStreamId();

    public String getUfrag();

    public String getPassword();

    public String getFoundation();

    public ComponentType getComponentType();

    public String getAddress();

    public int getPort();

    public int getPriority();

    public TransportType getTransportType();

    public CandidateType getType();

    public String getRelatedAddress();

    public int getRelatedPort();

    public enum ComponentType {
        RTP, RTCP
    }

    public enum TransportType {
        UDP, TCP_ACTIVE, TCP_PASSIVE, TCP_SO
    }

    public enum CandidateType {
        HOST, SERVER_REFLEXIVE, PEER_REFLEXIVE, RELAY
    }
}
