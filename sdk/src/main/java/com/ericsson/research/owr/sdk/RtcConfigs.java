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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RtcConfigs {
    private static final String TAG = "RtcConfigs";

    private RtcConfigs() {
    }

    public static RtcConfig defaultConfig(String stunServerUrl) {
        return new Default(stunServerUrl);
    }

    public static RtcConfig defaultConfig(Collection<RtcConfig.HelperServer> helperServers) {
        return new Default(helperServers);
    }

    private static class Default extends RtcConfig {
        private static final List<RtcPayload> sDefaultVideoPayloads = new ArrayList<>(4);
        static {
            sDefaultVideoPayloads.add(new RtcPayloadImpl(103, "H264", 90000, new HashMap<String, Object>(){{
                put("packetization-mode", 1);
            }}, 0, false, true, true));
            sDefaultVideoPayloads.add(new RtcPayloadImpl(123, "RTX", 90000, new HashMap<String, Object>(){{
                put("apt", 103);
                put("rtx-time", 200);
            }}, 0, false, false, false));
            sDefaultVideoPayloads.add(new RtcPayloadImpl(100, "VP8", 90000, null, 0, true, true, true));
            sDefaultVideoPayloads.add(new RtcPayloadImpl(120, "RTX", 90000, new HashMap<String, Object>(){{
                put("apt", 100);
                put("rtx-time", 200);
            }}, 0, false, false, false));
        }

        private static final List<RtcPayload> sDefaultAudioPayloads = new ArrayList<>(3);
        static {
            sDefaultAudioPayloads.add(new RtcPayloadImpl(111, "OPUS", 48000, null, 2, false, false, false));
            sDefaultAudioPayloads.add(new RtcPayloadImpl(8, "PCMA", 8000, null, 1, false, false, false));
            sDefaultAudioPayloads.add(new RtcPayloadImpl(0, "PCMU", 8000, null, 1, false, false, false));
        }

        private final Collection<HelperServer> mHelperServers;

        private Default(Collection<HelperServer> helperServers) {
            mHelperServers = helperServers;
        }

        private Default(String stunServerUrl) {
            String[] split = stunServerUrl.split(":");
            if (split.length < 1 || split.length > 2) {
                throw new IllegalArgumentException("invalid stun server url: " + stunServerUrl);
            }
            final int port;
            if (split.length == 2) {
                port = Integer.parseInt(split[1]);
            } else {
                port = 3478;
            }
            mHelperServers = Arrays.asList(new HelperServer(HelperServerType.STUN, split[0], port, "", ""));
        }

        @Override
        protected List<RtcPayload> getDefaultVideoPayloads() {
            return sDefaultVideoPayloads;
        }

        @Override
        protected List<RtcPayload> getDefaultAudioPayloads() {
            return sDefaultAudioPayloads;
        }

        @Override
        protected boolean shouldRespectRemotePayloadOrder() {
            return false;
        }

        @Override
        protected Collection<HelperServer> getHelperServers() {
            return mHelperServers;
        }
    }
}
