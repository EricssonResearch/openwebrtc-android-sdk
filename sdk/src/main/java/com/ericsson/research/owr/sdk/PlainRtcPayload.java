/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import java.util.Collections;
import java.util.Map;

class PlainRtcPayload implements RtcPayload {
    private final int mPayloadType;
    private final String mEncodingName;
    private final int mClockRate;
    private final Map<String, Object> mParameters;

    // Audio only
    private final int mChannels;

    // Video only
    private final boolean mNack;
    private final boolean mNackPli;
    private final boolean mCcmFir;

    PlainRtcPayload(int payloadType, String encodingName, int clockRate, Map<String, Object> parameters, int channels, boolean nack, boolean nackPli, boolean ccmFir) {
        mPayloadType = payloadType;
        mEncodingName = encodingName;
        mClockRate = clockRate;
        mParameters = parameters == null ? null : Collections.unmodifiableMap(parameters);
        mChannels = channels;
        mNack = nack;
        mNackPli = nackPli;
        mCcmFir = ccmFir;
    }

    @Override
    public int getPayloadType() {
        return mPayloadType;
    }

    @Override
    public String getEncodingName() {
        return mEncodingName;
    }

    @Override
    public int getClockRate() {
        return mClockRate;
    }

    @Override
    public Map<String, Object> getParameters() {
        return mParameters;
    }

    @Override
    public int getChannels() {
        return mChannels;
    }

    @Override
    public boolean isNack() {
        return mNack;
    }

    @Override
    public boolean isNackPli() {
        return mNackPli;
    }

    @Override
    public boolean isCcmFir() {
        return mCcmFir;
    }
}
