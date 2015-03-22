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

import com.ericsson.research.owr.AudioPayload;
import com.ericsson.research.owr.CodecType;
import com.ericsson.research.owr.HelperServerType;
import com.ericsson.research.owr.VideoPayload;

import java.util.Arrays;
import java.util.Collection;
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
        private static final List<VideoPayload> sDefaultVideoPayloads = Arrays.asList(
                new VideoPayload(CodecType.H264, 103, 90000, true, true),
                new VideoPayload(CodecType.VP8, 100, 90000, true, true)
        );

        private static final List<AudioPayload> sDefaultAudioPayloads = Arrays.asList(
                new AudioPayload(CodecType.OPUS, 111, 48000, 2),
                new AudioPayload(CodecType.PCMA, 8, 8000, 1),
                new AudioPayload(CodecType.PCMU, 0, 8000, 1)
        );

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
        List<VideoPayload> getDefaultVideoPayloads() {
            return sDefaultVideoPayloads;
        }

        @Override
        List<AudioPayload> getDefaultAudioPayloads() {
            return sDefaultAudioPayloads;
        }

        @Override
        Collection<HelperServer> getHelperServers() {
            return mHelperServers;
        }
    }
}
