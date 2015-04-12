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

import com.ericsson.research.owr.HelperServerType;

import java.util.Collection;
import java.util.List;

/**
 * An interface masquerading as an abstract class that allows configuration of an RtcSession.
 * The reason it's an abstract class is to be able to make the abstract methods package-private
 */
public abstract class RtcConfig {
    protected RtcConfig() {}

    /**
     * Implementations should return a list of payloads, ordered by most-preferred to least-preferred.
     * @return a list of payload
     */
    protected abstract List<RtcPayload> getDefaultVideoPayloads();

    /**
     * Implementations should return a list of payloads, ordered by most-preferred to least-preferred.
     * @return a list of payload
     */
    protected abstract List<RtcPayload> getDefaultAudioPayloads();

    /**
     * @return true if the send payload should be determined by the order in
     * which the payloads are listed in the remote session description. Otherwise the order in the local
     * configuration will be used.
     */
    protected abstract boolean shouldRespectRemotePayloadOrder();

    /**
     * Implementations should return a list of helper servers that are used for ICE.
     * @return a collection of helper servers
     */
    protected abstract Collection<HelperServer> getHelperServers();

    public static class HelperServer {
        private final HelperServerType mType;
        private final String mAddress;
        private final int mPort;
        private final String mUsername;
        private final String mPassword;

        public HelperServer(HelperServerType type, String address, int port, String username, String password) {
            mType = type;
            mAddress = address;
            mPort = port;
            mUsername = username;
            mPassword = password;
        }

        public HelperServerType getType() {
            return mType;
        }

        public String getAddress() {
            return mAddress;
        }

        public int getPort() {
            return mPort;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getPassword() {
            return mPassword;
        }
    }
}
