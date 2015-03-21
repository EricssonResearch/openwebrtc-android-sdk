/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import java.util.Map;

public interface RtcPayload {
    public int getPayloadType();

    public String getEncodingName();

    public int getClockRate();

    public Map<String, Object> getParameters();

    // Audio only
    public int getChannels();

    // Video only
    public boolean isNack();

    public boolean isNackPli();

    public boolean isCcmFir();
}
