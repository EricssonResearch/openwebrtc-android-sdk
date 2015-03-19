/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

public enum DescriptionType {
    INBOUND_OFFER(false, false), OUTBOUND_OFFER(true, false), INBOUND_ANSWER(false, true), OUTBOUND_ANSWER(true, true);

    private final boolean mIsOutbound;
    private final boolean mIsAnswer;

    private DescriptionType(boolean isOutbound, boolean isAnswer) {
        mIsOutbound = isOutbound;
        mIsAnswer = isAnswer;
    }

    public boolean isInbound() {
        return !mIsOutbound;
    }

    public boolean isOffer() {
        return !mIsAnswer;
    }

    public boolean isOutbound() {
        return mIsOutbound;
    }

    public boolean isAnswer() {
        return mIsAnswer;
    }
}
