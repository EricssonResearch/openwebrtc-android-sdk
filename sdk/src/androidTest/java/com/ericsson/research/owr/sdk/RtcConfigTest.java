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

import android.test.AndroidTestCase;

import com.ericsson.research.owr.HelperServerType;

import java.util.Arrays;
import java.util.List;

public class RtcConfigTest extends AndroidTestCase {
    private static List<String> sValidStunUrls = Arrays.asList(
            "stun.example.com",
            "stun.example.com:12345"
    );

    private static List<String> sInvalidStunUrls = Arrays.asList(
            "stun.example.com:12345/path",
            "stun:stun.example.com:12345",
            "stun:stun.example.com"
    );

    public void testUrls() {
        for (String url : sValidStunUrls) {
            RtcConfigs.defaultConfig(url);
        }
        for (String url : sInvalidStunUrls) {
            try {
                RtcConfigs.defaultConfig(url);
                throw new RuntimeException("should not be reached");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testDefaultConfig() {
        RtcConfig config = RtcConfigs.defaultConfig("stun.example.com:12345");
        assertEquals(3, config.getDefaultAudioPayloads().size());
        assertEquals(3, config.getDefaultVideoPayloads().size());
        assertEquals(false, config.shouldRespectRemotePayloadOrder());
        assertEquals(1, config.getHelperServers().size());
        RtcConfig.HelperServer helper = config.getHelperServers().iterator().next();
        assertEquals("stun.example.com", helper.getAddress());
        assertEquals(12345, helper.getPort());
        assertEquals("", helper.getUsername());
        assertEquals("", helper.getPassword());
        assertEquals(HelperServerType.STUN, helper.getType());
        RtcConfig config2 = RtcConfigs.defaultConfig(Arrays.asList(
                new RtcConfig.HelperServer(HelperServerType.TURN_TCP, "turn.example.com", 1234, "asd", "123")
        ));
        RtcConfig.HelperServer helper2 = config2.getHelperServers().iterator().next();
        assertEquals("turn.example.com", helper2.getAddress());
        assertEquals(1234, helper2.getPort());
        assertEquals("asd", helper2.getUsername());
        assertEquals("123", helper2.getPassword());
        assertEquals(HelperServerType.TURN_TCP, helper2.getType());
        assertSame(config.getDefaultAudioPayloads(), config2.getDefaultAudioPayloads());
        assertSame(config.getDefaultVideoPayloads(), config2.getDefaultVideoPayloads());
    }
}
