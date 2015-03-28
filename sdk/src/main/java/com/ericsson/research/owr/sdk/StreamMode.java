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

public enum StreamMode {
    SEND_RECEIVE(true, true), SEND_ONLY(true, false), RECEIVE_ONLY(false, true), INACTIVE(false, false);
    private final boolean mReceive;
    private final boolean mSend;

    StreamMode(boolean send, boolean receive) {
        mSend = send;
        mReceive = receive;
    }

    public boolean wantReceive() {
        return mReceive;
    }

    public boolean wantSend() {
        return mSend;
    }

    public static StreamMode get(boolean wantSend, boolean wantReceive) {
        if (wantSend && wantReceive) {
           return SEND_RECEIVE;
        } else if (wantSend) {
           return SEND_ONLY;
        } else if (wantReceive) {
           return RECEIVE_ONLY;
        } else {
           return INACTIVE;
        }
    }

    public StreamMode reverse(boolean wantSend, boolean wantReceive) {
        switch (this) {
            case INACTIVE:
                return StreamMode.INACTIVE;
            case SEND_ONLY:
                if (wantReceive) {
                    return StreamMode.RECEIVE_ONLY;
                } else {
                    return StreamMode.INACTIVE;
                }
            case RECEIVE_ONLY:
                if (wantSend) {
                    return StreamMode.SEND_ONLY;
                } else {
                    return StreamMode.INACTIVE;
                }
            case SEND_RECEIVE:
                if (wantSend && wantReceive) {
                    return StreamMode.SEND_RECEIVE;
                } else if (wantSend) {
                    return StreamMode.SEND_ONLY;
                } else if (wantReceive) {
                    return StreamMode.RECEIVE_ONLY;
                } else {
                    return StreamMode.INACTIVE;
                }
            default:
                return StreamMode.INACTIVE;
        }
    }
}
