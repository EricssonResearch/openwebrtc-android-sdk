/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import com.ericsson.research.owr.CandidateType;
import com.ericsson.research.owr.ComponentType;
import com.ericsson.research.owr.TransportType;

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
}
