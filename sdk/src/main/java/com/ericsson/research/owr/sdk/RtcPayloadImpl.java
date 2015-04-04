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
import java.util.Map;

class RtcPayloadImpl implements RtcPayload {
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

    RtcPayloadImpl(int payloadType, String encodingName, int clockRate, Map<String, Object> parameters, int channels, boolean nack, boolean nackPli, boolean ccmFir) {
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
