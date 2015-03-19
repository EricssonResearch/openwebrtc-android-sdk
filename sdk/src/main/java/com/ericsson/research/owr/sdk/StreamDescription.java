/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import com.ericsson.research.owr.Payload;

import java.util.List;

/**
 * An interface for describing a stream that is a part of a rtc session
 */
public interface StreamDescription {
    public String getId();

    public StreamType getStreamType();

    public StreamMode getMode();

    public String getUfrag();

    public String getPassword();

    public List<RtcCandidate> getCandidates();

    public String getDtlsSetup();

    public String getFingerprint();

    public String getFingerprintHashFunction();

    // media only
    public String getCname();

    public boolean isRtcpMux();

    public List<Long> getSsrcs();

    public List<Payload> getPayloads();

    // data only
    public int getSctpPort();

    public int getMessageMaxSize();

    public String getAppLabel();
}
