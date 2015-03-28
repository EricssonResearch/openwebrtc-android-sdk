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

import android.test.MoreAsserts;
import android.util.Pair;

import com.ericsson.research.owr.AudioPayload;
import com.ericsson.research.owr.Candidate;
import com.ericsson.research.owr.CandidateType;
import com.ericsson.research.owr.CodecType;
import com.ericsson.research.owr.ComponentType;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Owr;
import com.ericsson.research.owr.Payload;
import com.ericsson.research.owr.TransportType;
import com.ericsson.research.owr.VideoPayload;

import junit.framework.TestCase;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class UtilsTests extends TestCase {
    static {
        Owr.init();
    }

    private static final List<RtcPayload> sDefaultVideoPayloads = new ArrayList<>();

    static {
        sDefaultVideoPayloads.add(new PlainRtcPayload(103, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sDefaultVideoPayloads.add(new PlainRtcPayload(123, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 103);
            put("rtx-time", 200);
        }}, 0, false, false, false));
        sDefaultVideoPayloads.add(new PlainRtcPayload(100, "VP8", 90000, null, 0, true, true, true));
        sDefaultVideoPayloads.add(new PlainRtcPayload(120, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 100);
            put("rtx-time", 200);
        }}, 0, false, false, false));
    }

    private static final List<RtcPayload> sDefaultVideoPayloadsNoVp8Rtx = new ArrayList<>();
    static {
        sDefaultVideoPayloadsNoVp8Rtx.add(new PlainRtcPayload(103, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sDefaultVideoPayloadsNoVp8Rtx.add(new PlainRtcPayload(123, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 103);
            put("rtx-time", 200);
        }}, 0, false, false, false));
        sDefaultVideoPayloadsNoVp8Rtx.add(new PlainRtcPayload(100, "VP8", 90000, null, 0, true, true, true));
    }

    private static final List<RtcPayload> sDefaultVideoPayloadsNoH264Rtx = new ArrayList<>();
    static {
        sDefaultVideoPayloadsNoH264Rtx.add(new PlainRtcPayload(103, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sDefaultVideoPayloadsNoH264Rtx.add(new PlainRtcPayload(100, "VP8", 90000, null, 0, true, true, true));
        sDefaultVideoPayloadsNoH264Rtx.add(new PlainRtcPayload(120, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 100);
            put("rtx-time", 200);
        }}, 0, false, false, false));
    }

    private static final List<RtcPayload> sValidPayloads1 = new ArrayList<>();
    static {
        sValidPayloads1.add(new PlainRtcPayload(110, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sValidPayloads1.add(new PlainRtcPayload(115, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 0);
        }}, 0, false, true, true));
        sValidPayloads1.add(new PlainRtcPayload(112, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 110);
            put("rtx-time", 200);
        }}, 0, false, false, false));
        sValidPayloads1.add(new PlainRtcPayload(117, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 115);
            put("rtx-time", 200);
        }}, 0, false, false, false));
    }

    private static final List<RtcPayload> sValidPayloads2 = new ArrayList<>();
    static {
        sValidPayloads2.add(new PlainRtcPayload(110, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
    }

    private static final List<RtcPayload> sValidPayloads3 = new ArrayList<>();
    static {
        sValidPayloads3.add(new PlainRtcPayload(110, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, false, true));
    }

    private static final List<RtcPayload> sValidPayloads4 = new ArrayList<>();
    static {
        sValidPayloads4.add(new PlainRtcPayload(110, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, false));
    }

    private static final List<RtcPayload> sValidPayloads5 = new ArrayList<>();
    static {
        sValidPayloads5.add(new PlainRtcPayload(110, "H264", 90000, new HashMap<String, Object>() {{
            put("packetization-mode", 1);
        }}, 0, false, false, false));
    }

    private static final List<RtcPayload> sValidPayloads6 = new ArrayList<>();
    static {
        sValidPayloads6.add(new PlainRtcPayload(110, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sValidPayloads6.add(new PlainRtcPayload(115, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 0);
        }}, 0, false, true, true));
        sValidPayloads6.add(new PlainRtcPayload(112, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 110);
            put("rtx-time", 200);
        }}, 0, false, true, true));
        sValidPayloads6.add(new PlainRtcPayload(117, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 115);
            put("rtx-time", 200);
        }}, 0, false, true, true));
        sValidPayloads6.add(new PlainRtcPayload(111, "VP8", 90000, null, 0, false, true, true));
        sValidPayloads6.add(new PlainRtcPayload(116, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 111);
            put("rtx-time", 200);
        }}, 0, false, true, true));
    }

    private static final List<RtcPayload> sDefaultAudioPayloads = new ArrayList<>();
    static {
        sDefaultAudioPayloads.add(new PlainRtcPayload(111, "OPUS", 48000, null, 2, false, false, false));
        sDefaultAudioPayloads.add(new PlainRtcPayload(8, "PCMA", 8000, null, 1, false, false, false));
        sDefaultAudioPayloads.add(new PlainRtcPayload(0, "PCMU", 8000, null, 1, false, false, false));
    }

    public void testTransformPayloads() {
        try {
            Utils.transformPayloads(null, MediaType.VIDEO);
            throw new RuntimeException("transformPayloads with null list should throw NPE");
        } catch (NullPointerException ignored) {
        }
        try {
            Utils.transformPayloads(sDefaultAudioPayloads, MediaType.UNKNOWN);
            throw new RuntimeException("transformPayloads with unknown media type should throw IAE");
        } catch (IllegalArgumentException ignored) {
        }
        List<Payload> transformedVideo = Utils.transformPayloads(sDefaultVideoPayloads, MediaType.VIDEO);
        assertEquals(2, transformedVideo.size());
        VideoPayload h264 = (VideoPayload) transformedVideo.get(0);
        assertEquals(CodecType.H264, h264.getCodecType());
        assertEquals(90000, h264.getClockRate());
        assertEquals(103, h264.getPayloadType());
        assertEquals(true, h264.getCcmFir());
        assertEquals(true, h264.getNackPli());
        assertTrue(h264.getMediaType().contains(MediaType.VIDEO));
        assertEquals(123, h264.getRtxPayloadType());
        assertEquals(200, h264.getRtxTime());

        VideoPayload vp8 = (VideoPayload) transformedVideo.get(1);
        assertEquals(CodecType.VP8, vp8.getCodecType());
        assertEquals(90000, vp8.getClockRate());
        assertEquals(100, vp8.getPayloadType());
        assertEquals(true, vp8.getCcmFir());
        assertEquals(true, vp8.getNackPli());
        assertTrue(vp8.getMediaType().contains(MediaType.VIDEO));
        assertEquals(120, vp8.getRtxPayloadType());
        assertEquals(200, vp8.getRtxTime());

        transformedVideo = Utils.transformPayloads(sDefaultVideoPayloadsNoVp8Rtx, MediaType.VIDEO);
        assertEquals(2, transformedVideo.size());

        vp8 = (VideoPayload) transformedVideo.get(1);
        assertEquals(CodecType.VP8, vp8.getCodecType());
        assertEquals(90000, vp8.getClockRate());
        assertEquals(100, vp8.getPayloadType());
        assertEquals(true, vp8.getCcmFir());
        assertEquals(true, vp8.getNackPli());
        assertTrue(vp8.getMediaType().contains(MediaType.VIDEO));
        assertEquals(-1, vp8.getRtxPayloadType());
        assertEquals(0, vp8.getRtxTime());

        List<Payload> transformedAudio = Utils.transformPayloads(sDefaultAudioPayloads, MediaType.AUDIO);
        assertEquals(3, transformedAudio.size());
        AudioPayload opus = (AudioPayload) transformedAudio.get(0);
        assertEquals(CodecType.OPUS, opus.getCodecType());
        assertEquals(48000, opus.getClockRate());
        assertEquals(111, opus.getPayloadType());
        assertEquals(2, opus.getChannels());
        assertTrue(opus.getMediaType().contains(MediaType.AUDIO));
        assertEquals(-1, opus.getRtxPayloadType());
        assertEquals(0, opus.getRtxTime());

        AudioPayload pcma = (AudioPayload) transformedAudio.get(1);
        assertEquals(CodecType.PCMA, pcma.getCodecType());
        assertEquals(8000, pcma.getClockRate());
        assertEquals(8, pcma.getPayloadType());
        assertEquals(1, pcma.getChannels());
        assertTrue(pcma.getMediaType().contains(MediaType.AUDIO));
        assertEquals(-1, pcma.getRtxPayloadType());
        assertEquals(0, pcma.getRtxTime());

        AudioPayload pcmu = (AudioPayload) transformedAudio.get(2);
        assertEquals(CodecType.PCMU, pcmu.getCodecType());
        assertEquals(8000, pcmu.getClockRate());
        assertEquals(0, pcmu.getPayloadType());
        assertEquals(1, pcmu.getChannels());
        assertTrue(pcmu.getMediaType().contains(MediaType.AUDIO));
        assertEquals(-1, pcmu.getRtxPayloadType());
        assertEquals(0, pcmu.getRtxTime());
    }

    public void testIntersectPayloads() {
        List<RtcPayload> emptyIntersection = Utils.intersectPayloads(sDefaultVideoPayloads, Collections.<RtcPayload>emptyList());
        MoreAsserts.assertEmpty(emptyIntersection);
    }

    public void testSimpleIntersection() {
        List<RtcPayload> intersection1 = Utils.intersectPayloads(sValidPayloads1, sDefaultVideoPayloads);
        assertEquals(2, intersection1.size());
        RtcPayload h2641 = intersection1.get(0);
        assertEquals("H264", h2641.getEncodingName());
        assertEquals(90000, h2641.getClockRate());
        assertEquals(110, h2641.getPayloadType());
        assertEquals(0, h2641.getChannels());
        assertEquals(1, h2641.getParameters().get("packetization-mode"));
        assertEquals(false, h2641.isNack());
        assertEquals(true, h2641.isNackPli());
        assertEquals(true, h2641.isCcmFir());
        RtcPayload h264rtx1 = intersection1.get(1);
        assertEquals("RTX", h264rtx1.getEncodingName());
        assertEquals(90000, h264rtx1.getClockRate());
        assertEquals(112, h264rtx1.getPayloadType());
        assertEquals(0, h264rtx1.getChannels());
        assertEquals(110, h264rtx1.getParameters().get("apt"));
        assertEquals(200, h264rtx1.getParameters().get("rtx-time"));
        assertEquals(false, h264rtx1.isNack());
        assertEquals(false, h264rtx1.isNackPli());
        assertEquals(false, h264rtx1.isCcmFir());
    }

    public void testh264NoRtxIntersection() {
        List<RtcPayload> intersection2 = Utils.intersectPayloads(sValidPayloads2, sDefaultVideoPayloads);
        assertEquals(1, intersection2.size());
        RtcPayload h2642 = intersection2.get(0);
        assertEquals("H264", h2642.getEncodingName());
        assertEquals(90000, h2642.getClockRate());
        assertEquals(110, h2642.getPayloadType());
        assertEquals(0, h2642.getChannels());
        assertEquals(1, h2642.getParameters().get("packetization-mode"));
        assertEquals(false, h2642.isNack());
        assertEquals(true, h2642.isNackPli());
        assertEquals(true, h2642.isCcmFir());
    }

    public void testNoNackPliIntersection() {
        List<RtcPayload> intersection3 = Utils.intersectPayloads(sValidPayloads3, sDefaultVideoPayloads);
        assertEquals(1, intersection3.size());
        assertEquals(false, intersection3.get(0).isNack());
        assertEquals(false, intersection3.get(0).isNackPli());
        assertEquals(true, intersection3.get(0).isCcmFir());
    }

    public void testNoCcmFirIntersection() {
        List<RtcPayload> intersection4 = Utils.intersectPayloads(sValidPayloads4, sDefaultVideoPayloads);
        assertEquals(1, intersection4.size());
        assertEquals(false, intersection4.get(0).isNack());
        assertEquals(true, intersection4.get(0).isNackPli());
        assertEquals(false, intersection4.get(0).isCcmFir());
    }

    public void testNoNackPliOrCcmFirIntersection() {
        List<RtcPayload> intersection5 = Utils.intersectPayloads(sValidPayloads5, sDefaultVideoPayloads);
        assertEquals(1, intersection5.size());
        assertEquals(false, intersection5.get(0).isNack());
        assertEquals(false, intersection5.get(0).isNackPli());
        assertEquals(false, intersection5.get(0).isCcmFir());
    }

    public void testFullRtxIntersection() {
        List<RtcPayload> intersection6 = Utils.intersectPayloads(sValidPayloads6, sDefaultVideoPayloads);
        assertEquals(4, intersection6.size());
        RtcPayload h264_6 = intersection6.get(0);
        assertEquals("H264", h264_6.getEncodingName());
        assertEquals(90000, h264_6.getClockRate());
        assertEquals(110, h264_6.getPayloadType());
        assertEquals(0, h264_6.getChannels());
        assertEquals(1, h264_6.getParameters().get("packetization-mode"));
        assertEquals(false, h264_6.isNack());
        assertEquals(true, h264_6.isNackPli());
        assertEquals(true, h264_6.isCcmFir());
        RtcPayload h264rtx_6 = intersection6.get(1);
        assertEquals("RTX", h264rtx_6.getEncodingName());
        assertEquals(90000, h264rtx_6.getClockRate());
        assertEquals(112, h264rtx_6.getPayloadType());
        assertEquals(0, h264rtx_6.getChannels());
        assertEquals(110, h264rtx_6.getParameters().get("apt"));
        assertEquals(200, h264rtx_6.getParameters().get("rtx-time"));
        assertEquals(false, h264rtx_6.isNack());
        assertEquals(true, h264rtx_6.isNackPli());
        assertEquals(true, h264rtx_6.isCcmFir());
        RtcPayload vp8_6 = intersection6.get(2);
        assertEquals("VP8", vp8_6.getEncodingName());
        assertEquals(90000, vp8_6.getClockRate());
        assertEquals(111, vp8_6.getPayloadType());
        assertEquals(0, vp8_6.getChannels());
        assertNull(vp8_6.getParameters());
        assertEquals(false, vp8_6.isNack());
        assertEquals(true, vp8_6.isNackPli());
        assertEquals(true, vp8_6.isCcmFir());
        RtcPayload vp8rtx_6 = intersection6.get(3);
        assertEquals("RTX", vp8rtx_6.getEncodingName());
        assertEquals(90000, vp8rtx_6.getClockRate());
        assertEquals(116, vp8rtx_6.getPayloadType());
        assertEquals(0, vp8rtx_6.getChannels());
        assertEquals(111, vp8rtx_6.getParameters().get("apt"));
        assertEquals(200, vp8rtx_6.getParameters().get("rtx-time"));
        assertEquals(false, vp8rtx_6.isNack());
        assertEquals(true, vp8rtx_6.isNackPli());
        assertEquals(true, vp8rtx_6.isCcmFir());
    }

    public void testFullH264RtxButNoVp8RtxIntersection () {
        List<RtcPayload> intersection6_b = Utils.intersectPayloads(sValidPayloads6, sDefaultVideoPayloadsNoVp8Rtx);
        assertEquals(3, intersection6_b.size());
        RtcPayload h26_b4_6_b = intersection6_b.get(0);
        assertEquals("H264", h26_b4_6_b.getEncodingName());
        assertEquals(90000, h26_b4_6_b.getClockRate());
        assertEquals(110, h26_b4_6_b.getPayloadType());
        assertEquals(0, h26_b4_6_b.getChannels());
        assertEquals(1, h26_b4_6_b.getParameters().get("packetization-mode"));
        assertEquals(false, h26_b4_6_b.isNack());
        assertEquals(true, h26_b4_6_b.isNackPli());
        assertEquals(true, h26_b4_6_b.isCcmFir());
        RtcPayload h26_b4rtx_6_b = intersection6_b.get(1);
        assertEquals("RTX", h26_b4rtx_6_b.getEncodingName());
        assertEquals(90000, h26_b4rtx_6_b.getClockRate());
        assertEquals(112, h26_b4rtx_6_b.getPayloadType());
        assertEquals(0, h26_b4rtx_6_b.getChannels());
        assertEquals(110, h26_b4rtx_6_b.getParameters().get("apt"));
        assertEquals(200, h26_b4rtx_6_b.getParameters().get("rtx-time"));
        assertEquals(false, h26_b4rtx_6_b.isNack());
        assertEquals(true, h26_b4rtx_6_b.isNackPli());
        assertEquals(true, h26_b4rtx_6_b.isCcmFir());
        RtcPayload vp8_6_b = intersection6_b.get(2);
        assertEquals("VP8", vp8_6_b.getEncodingName());
        assertEquals(90000, vp8_6_b.getClockRate());
        assertEquals(111, vp8_6_b.getPayloadType());
        assertEquals(0, vp8_6_b.getChannels());
        assertNull(vp8_6_b.getParameters());
        assertEquals(false, vp8_6_b.isNack());
        assertEquals(true, vp8_6_b.isNackPli());
        assertEquals(true, vp8_6_b.isCcmFir());
    }

    private static final List<RtcPayload> sDefaultVideoPayloadsRtxReordered = new ArrayList<>();
    static {
        sDefaultVideoPayloadsRtxReordered.add(new PlainRtcPayload(123, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 103);
            put("rtx-time", 200);
        }}, 0, false, false, false));
        sDefaultVideoPayloadsRtxReordered.add(new PlainRtcPayload(103, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sDefaultVideoPayloadsRtxReordered.add(new PlainRtcPayload(100, "VP8", 90000, null, 0, true, true, true));
        sDefaultVideoPayloadsRtxReordered.add(new PlainRtcPayload(120, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 100);
            put("rtx-time", 200);
        }}, 0, false, false, false));
    }

    private static final List<RtcPayload> sDefaultVideoPayloadsRtxReorderedVp8 = new ArrayList<>();
    static {
        sDefaultVideoPayloadsRtxReorderedVp8.add(new PlainRtcPayload(123, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 103);
            put("rtx-time", 200);
        }}, 0, false, false, false));
        sDefaultVideoPayloadsRtxReorderedVp8.add(new PlainRtcPayload(100, "VP8", 90000, null, 0, true, true, true));
        sDefaultVideoPayloadsRtxReorderedVp8.add(new PlainRtcPayload(103, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sDefaultVideoPayloadsRtxReorderedVp8.add(new PlainRtcPayload(120, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 100);
            put("rtx-time", 200);
        }}, 0, false, false, false));
    }

    private static final List<RtcPayload> sDefaultVideoPayloadsVp8RtxReorderedFirst = new ArrayList<>();
    static {
        sDefaultVideoPayloadsVp8RtxReorderedFirst.add(new PlainRtcPayload(120, "RTX", 90000, new HashMap<String, Object>(){{
            put("apt", 100);
            put("rtx-time", 200);
        }}, 0, false, false, false));
        sDefaultVideoPayloadsVp8RtxReorderedFirst.add(new PlainRtcPayload(103, "H264", 90000, new HashMap<String, Object>(){{
            put("packetization-mode", 1);
        }}, 0, false, true, true));
        sDefaultVideoPayloadsVp8RtxReorderedFirst.add(new PlainRtcPayload(100, "VP8", 90000, null, 0, true, true, true));
    }

    public void testPreferredPayloadSelection() {
        List<RtcPayload> result;

        result = Utils.selectPreferredPayload(sDefaultVideoPayloads);
        assertEquals(2, result.size());
        assertEquals(103, result.get(0).getPayloadType());
        assertEquals(123, result.get(1).getPayloadType());

        result = Utils.selectPreferredPayload(sDefaultVideoPayloadsNoVp8Rtx);
        assertEquals(2, result.size());
        assertEquals(103, result.get(0).getPayloadType());
        assertEquals(123, result.get(1).getPayloadType());

        result = Utils.selectPreferredPayload(sDefaultVideoPayloadsNoH264Rtx);
        assertEquals(1, result.size());
        assertEquals(103, result.get(0).getPayloadType());

        result = Utils.selectPreferredPayload(sValidPayloads2);
        assertEquals(1, result.size());
        assertEquals(110, result.get(0).getPayloadType());

        result = Utils.selectPreferredPayload(sDefaultVideoPayloadsRtxReordered);
        assertEquals(2, result.size());
        assertEquals(103, result.get(0).getPayloadType());
        assertEquals(123, result.get(1).getPayloadType());

        result = Utils.selectPreferredPayload(sDefaultVideoPayloadsRtxReorderedVp8);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getPayloadType());
        assertEquals(120, result.get(1).getPayloadType());

        result = Utils.selectPreferredPayload(sDefaultVideoPayloadsVp8RtxReorderedFirst);
        assertEquals(1, result.size());
        assertEquals(103, result.get(0).getPayloadType());
    }

    private static String sFingerprint = "55:20:0F:C6:8D:99:19:A2:09:AF:F3:64:C9:43:53:B6:C8:E0:C7:C9:B6:20:B7:91:11:E9:8B:77:57:D6:43:9B";
    private static String sFingerprintHashFunction = "sha-256";
    private static String sPem = "-----BEGIN CERTIFICATE-----\n" +
            "MIIBmTCCAQKgAwIBAgIEf/zbODANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZX\n" +
            "ZWJSVEMwHhcNMTUwMzIzMTA1NzQxWhcNMTUwNDIyMTA1NzQxWjARMQ8wDQYDVQQD\n" +
            "DAZXZWJSVEMwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMJ4CHcE8NNCWIMO\n" +
            "uWs1wF79LJ46kfLZzNzCWaGzc0PdCo8pdjfS1cWvnleXdNaIg8qpLqW9C71Jx3A5\n" +
            "gx2HUCDopX/TOslhVk5OnjSknTPR5qq3JhF/s+/qxFkd0y3sbAVUTocQ0uiAb+eK\n" +
            "zzY8x8rvw6ge5A7/hCA2i3fXdaHxAgMBAAEwDQYJKoZIhvcNAQELBQADgYEApknp\n" +
            "WGIXxUMaTQq/ULCJhPXzB+a7eBZtOL8xbe5OiHfD+lJxvifh9pZXH6n6yw+IkcxY\n" +
            "IHiheIjRdcEngg1K7RAZf2dg2utWPj6U3KrZ6vlqU1EYsb/26zV4DZUjtfS5iQJL\n" +
            "HGZ/v03ZrrOkJCrWC1ISmDDJcONRiZcpMV2V3mI=\n" +
            "-----END CERTIFICATE-----";

    public void testRandomString() {
        for (int i = 0; i < 10; i++) {
            String str = Utils.randomString(i);
            assertEquals(str, i, str.length());
        }
    }

    public void testFingerprint() {
        String fingerprint = Utils.fingerprintFromPem(sPem, sFingerprintHashFunction);
        assertEquals(sFingerprint, fingerprint);
        fingerprint = Utils.fingerprintFromPem(sPem + "\n", sFingerprintHashFunction);
        assertEquals(sFingerprint, fingerprint);
        fingerprint = Utils.fingerprintFromPem(sPem + "\n\n", sFingerprintHashFunction);
        assertEquals(sFingerprint, fingerprint);
        String fingerprintInvalidPem = Utils.fingerprintFromPem("derp derp", sFingerprintHashFunction);
        assertNull(fingerprintInvalidPem);
        try {
            Utils.fingerprintFromPem(sPem, "coolcrypt-2000");
            throw new RuntimeException("should not be reached");
        } catch (RuntimeException e) {
            if (!(e.getCause() instanceof NoSuchAlgorithmException)) {
                throw new RuntimeException("invalid exception", e);
            }
        }
    }

    public void testStreamMode() {
        assertSame(StreamMode.SEND_RECEIVE, StreamMode.get(true, true));
        assertSame(StreamMode.SEND_ONLY, StreamMode.get(true, false));
        assertSame(StreamMode.RECEIVE_ONLY, StreamMode.get(false, true));
        assertSame(StreamMode.INACTIVE, StreamMode.get(false, false));
    }

    private static final String[] candidateLines = new String[] {
            "candidate:0 1 UDP 2130379007 172.20.10.2 51850 typ host",
            "candidate:1 2 TCP 2130379006 172.20.10.2 9 typ host",
            "candidate:2 1 UDP 1694236671 90.237.24.157 35856 typ srflx raddr 172.20.10.2 rport 51850",
            "candidate:3 2 TCP 1694236670 90.237.24.157 9 typ srflx raddr 172.20.10.2 rport 61466",
            "candidate:4 2 TCP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 rport 61466 tcptype active",
            "candidate:5 2 TCP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 rport 61466 tcptype passive",
            "candidate:6 2 TCP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 rport 61466 tcptype so",
            "candidate:7 1 UDP 100401151 192.36.158.14 55300 typ relay raddr 192.36.158.14 rport 55300",
            "candidate:8 2 TCP 100401150 192.36.158.14 9 typ relay raddr 192.36.158.14 rport 61720",
    };

    public void testCandidateTransform() {
        List<Candidate> candidates = new LinkedList<>();
        for (String candidateLine : candidateLines) {
            RtcCandidate rtcCandidate = RtcCandidates.fromSdpAttribute(candidateLine);
            assertNotNull(candidateLine, rtcCandidate);
            Candidate candidate = Utils.transformCandidate(rtcCandidate);
            assertNotNull(candidateLine, candidate);
            candidates.add(candidate);
        }

        Candidate candidate0 = candidates.get(0);
        assertEquals("0", candidate0.getFoundation());
        assertEquals(ComponentType.RTP, candidate0.getComponentType());
        assertEquals(TransportType.UDP, candidate0.getTransportType());
        assertEquals(2130379007, candidate0.getPriority());
        assertEquals("172.20.10.2", candidate0.getAddress());
        assertEquals(51850, candidate0.getPort());
        assertEquals(CandidateType.HOST, candidate0.getType());

        assertEquals(TransportType.TCP_ACTIVE, candidates.get(1).getTransportType());
        assertEquals(TransportType.TCP_ACTIVE, candidates.get(4).getTransportType());
        assertEquals(TransportType.TCP_PASSIVE, candidates.get(5).getTransportType());
        assertEquals(TransportType.TCP_SO, candidates.get(6).getTransportType());

        RtcCandidate rtcCandidate = new PlainRtcCandidate(0, null, "asd", "123", "234",
                RtcCandidate.ComponentType.RTP, RtcCandidate.TransportType.UDP, 3, "127.1", 13,
                RtcCandidate.CandidateType.PEER_REFLEXIVE, "192.1", 14);
        Candidate candidate = Utils.transformCandidate(rtcCandidate);

        assertEquals("asd", candidate.getUfrag());
        assertEquals("123", candidate.getPassword());
        assertEquals("234", candidate.getFoundation());
        assertEquals(ComponentType.RTP, candidate.getComponentType());
        assertEquals(TransportType.UDP, candidate.getTransportType());
        assertEquals(3, candidate.getPriority());
        assertEquals("127.1", candidate.getAddress());
        assertEquals(13, candidate.getPort());
        assertEquals(CandidateType.PEER_REFLEXIVE, candidate.getType());
        assertEquals("192.1", candidate.getBaseAddress());
        assertEquals(14, candidate.getBasePort());
    }

    private SessionDescription sAudioOnlyOffer = new SessionDescriptionMock(SessionDescription.Type.OFFER,
            new StreamDescriptionMock(StreamType.AUDIO)
    );

    private SessionDescription s10AudioOffer = new SessionDescriptionMock(SessionDescription.Type.OFFER,
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO)
    );

    private SessionDescription s10EachTypeOfferOrdered = new SessionDescriptionMock(SessionDescription.Type.OFFER,
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA)
    );

    private SessionDescription s10EachTypeOfferUnordered = new SessionDescriptionMock(SessionDescription.Type.OFFER,
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA)
    );

    private SessionDescription s5EachTypeOfferOrdered = new SessionDescriptionMock(SessionDescription.Type.OFFER,
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.DATA)
    );

    private SessionDescription s5EachTypeOfferUnordered = new SessionDescriptionMock(SessionDescription.Type.OFFER,
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.VIDEO),
            new StreamDescriptionMock(StreamType.DATA),
            new StreamDescriptionMock(StreamType.AUDIO),
            new StreamDescriptionMock(StreamType.DATA)
    );

    private List<? extends StreamSet.Stream> sSingleAudioStream = Arrays.asList(new StreamMock(StreamType.AUDIO));

    private List<? extends StreamSet.Stream> s10AudioStreams = Arrays.asList(
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO)
    );

    private List<? extends StreamSet.Stream> s10EachTypeStreamsOrdered = Arrays.asList(
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA)
    );

    private List<? extends StreamSet.Stream> s10EachTypeStreamsUnordered = Arrays.asList(
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA)
    );

    private List<? extends StreamSet.Stream> s5EachTypeStreamsOrdered = Arrays.asList(
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA)
    );

    private List<? extends StreamSet.Stream> s5EachTypeStreamsUnordered = Arrays.asList(
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.VIDEO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.DATA),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.AUDIO),
            new StreamMock(StreamType.VIDEO)
    );

    private ResultCount countResult(List<Pair<StreamDescription, StreamSet.Stream>> result) {
        ResultCount count = new ResultCount();

        for (Pair<StreamDescription, StreamSet.Stream> pair : result) {
            if (pair.second != null) {
                assertSame(pair.first.getType(), pair.second.getType());
            }
            switch (pair.first.getType()) {
                case AUDIO:
                    if (pair.second == null) {
                        count.nullAudio += 1;
                    } else {
                        count.activeAudio += 1;
                    }
                    break;
                case VIDEO:
                    if (pair.second == null) {
                        count.nullVideo += 1;
                    } else {
                        count.activeVideo += 1;
                    }
                    break;
                case DATA:
                    if (pair.second == null) {
                        count.nullData += 1;
                    } else {
                        count.activeData += 1;
                    }
                    break;
            }
        }
        return count;
    }

    private void countInactive(final ResultCount count, final List<? extends StreamSet.Stream> streams) {
        for (StreamSet.Stream stream : streams) {
            switch (stream.getType()) {
                case AUDIO:
                    if (((StreamMock) stream).getStreamMode() == StreamMode.INACTIVE) {
                        count.inactiveAudio += 1;
                    }
                    break;
                case VIDEO:
                    if (((StreamMock) stream).getStreamMode() == StreamMode.INACTIVE) {
                        count.inactiveVideo += 1;
                    }
                    break;
                case DATA:
                    if (((StreamMock) stream).getStreamMode() == StreamMode.INACTIVE) {
                        count.inactiveData += 1;
                    }
                    break;
            }
        }
    }

    private class ResultCount {
        public int activeAudio;
        public int activeVideo;
        public int activeData;
        public int inactiveAudio;
        public int inactiveVideo;
        public int inactiveData;
        public int nullAudio;
        public int nullVideo;
        public int nullData;

        public void assertAudioCount(int activeCount, int inactiveCount, int nullCount) {
            assertEquals(activeCount, activeAudio);
            assertEquals(inactiveCount, inactiveAudio);
            assertEquals(nullCount, nullAudio);
        }

        public void assertVideoCount(int activeCount, int inactiveCount, int nullCount) {
            assertEquals(activeCount, activeVideo);
            assertEquals(inactiveCount, inactiveVideo);
            assertEquals(nullCount, nullVideo);
        }

        public void assertDataCount(int activeCount, int inactiveCount, int nullCount) {
            assertEquals(activeCount, activeData);
            assertEquals(inactiveCount, inactiveData);
            assertEquals(nullCount, nullData);
        }

        public void assertAllCounts(int activeCount, int inactiveCount, int nullCount) {
            assertAudioCount(activeCount, inactiveCount, nullCount);
            assertVideoCount(activeCount, inactiveCount, nullCount);
            assertDataCount(activeCount, inactiveCount, nullCount);
        }
    }

    private ResultCount runResolveCounter(SessionDescription remoteDescription, List<? extends StreamSet.Stream> streams) {
        for (StreamSet.Stream stream : streams) {
            stream.setStreamMode(StreamMode.SEND_RECEIVE);
        }
        ResultCount count = countResult(Utils.resolveOfferedStreams(remoteDescription, streams));
        countInactive(count, streams);
        return count;
    }

    public void testResolveOfferedStreams() {
        List<Pair<StreamDescription, StreamSet.Stream>> result;
        ResultCount count;
        StreamMock streamMock;

        result = Utils.resolveOfferedStreams(sAudioOnlyOffer, sSingleAudioStream);
        assertNotNull(result);
        assertEquals(1, result.size());
        streamMock = (StreamMock) result.get(0).second;
        assertSame(StreamType.AUDIO, result.get(0).first.getType());
        assertSame(StreamType.AUDIO, streamMock.getType());
        assertNull(streamMock.getStreamMode());

        try {
            Utils.resolveOfferedStreams(new SessionDescriptionMock(SessionDescription.Type.ANSWER,
                    new StreamDescriptionMock(StreamType.AUDIO)), sSingleAudioStream);
            throw new RuntimeException("should not be reached");
        } catch (IllegalArgumentException ignored) {
        }

        // ordered
        count = runResolveCounter(s10EachTypeOfferOrdered, s10EachTypeStreamsOrdered);
        count.assertAllCounts(10, 0, 0);

        count = runResolveCounter(s5EachTypeOfferOrdered, s10EachTypeStreamsOrdered);
        count.assertAllCounts(5, 5, 0);

        count = runResolveCounter(s10EachTypeOfferOrdered, s5EachTypeStreamsOrdered);
        count.assertAllCounts(5, 0, 5);

        // unordered
        count = runResolveCounter(s10EachTypeOfferUnordered, s10EachTypeStreamsUnordered);
        count.assertAllCounts(10, 0, 0);

        count = runResolveCounter(s5EachTypeOfferUnordered, s10EachTypeStreamsUnordered);
        count.assertAllCounts(5, 5, 0);

        count = runResolveCounter(s10EachTypeOfferUnordered, s5EachTypeStreamsUnordered);
        count.assertAllCounts(5, 0, 5);

        // descriptions ordered
        count = runResolveCounter(s10EachTypeOfferOrdered, s10EachTypeStreamsUnordered);
        count.assertAllCounts(10, 0, 0);

        count = runResolveCounter(s5EachTypeOfferOrdered, s10EachTypeStreamsUnordered);
        count.assertAllCounts(5, 5, 0);

        count = runResolveCounter(s10EachTypeOfferOrdered, s5EachTypeStreamsUnordered);
        count.assertAllCounts(5, 0, 5);

        // streams ordered
        count = runResolveCounter(s10EachTypeOfferUnordered, s10EachTypeStreamsOrdered);
        count.assertAllCounts(10, 0, 0);

        count = runResolveCounter(s5EachTypeOfferUnordered, s10EachTypeStreamsOrdered);
        count.assertAllCounts(5, 5, 0);

        count = runResolveCounter(s10EachTypeOfferUnordered, s5EachTypeStreamsOrdered);
        count.assertAllCounts(5, 0, 5);

        // missing some streams
        count = runResolveCounter(s10EachTypeOfferOrdered, s10AudioStreams);
        count.assertAudioCount(10, 0, 0);
        count.assertVideoCount(0, 0, 10);
        count.assertDataCount(0, 0, 10);

        count = runResolveCounter(s10EachTypeOfferUnordered, s10AudioStreams);
        count.assertAudioCount(10, 0, 0);
        count.assertVideoCount(0, 0, 10);
        count.assertDataCount(0, 0, 10);

        count = runResolveCounter(s5EachTypeOfferOrdered, s10AudioStreams);
        count.assertAudioCount(5, 5, 0);
        count.assertVideoCount(0, 0, 5);
        count.assertDataCount(0, 0, 5);

        count = runResolveCounter(s5EachTypeOfferUnordered, s10AudioStreams);
        count.assertAudioCount(5, 5, 0);
        count.assertVideoCount(0, 0, 5);
        count.assertDataCount(0, 0, 5);

        // missing some descriptions
        count = runResolveCounter(s10AudioOffer, s10EachTypeStreamsOrdered);
        count.assertAudioCount(10, 0, 0);
        count.assertVideoCount(0, 10, 0);
        count.assertDataCount(0, 10, 0);

        count = runResolveCounter(s10AudioOffer, s10EachTypeStreamsUnordered);
        count.assertAudioCount(10, 0, 0);
        count.assertVideoCount(0, 10, 0);
        count.assertDataCount(0, 10, 0);

        count = runResolveCounter(s10AudioOffer, s5EachTypeStreamsOrdered);
        count.assertAudioCount(5, 0, 5);
        count.assertVideoCount(0, 5, 0);
        count.assertDataCount(0, 5, 0);

        count = runResolveCounter(s10AudioOffer, s5EachTypeStreamsUnordered);
        count.assertAudioCount(5, 0, 5);
        count.assertVideoCount(0, 5, 0);
        count.assertDataCount(0, 5, 0);
    }


    private class SessionDescriptionMock implements SessionDescription {
        private Type mType;
        private List<StreamDescription> mStreamDescriptions;

        private SessionDescriptionMock(Type type, StreamDescription... streamDescriptions) {
            mType = type;
            mStreamDescriptions = Arrays.asList(streamDescriptions);
        }

        @Override
        public Type getType() {
            return mType;
        }

        @Override
        public List<StreamDescription> getStreamDescriptions() {
            return mStreamDescriptions;
        }

        @Override
        public String getSessionId() {
            return null;
        }

        @Override
        public boolean hasStreamType(final StreamType streamType) {
            return false;
        }
    }

    private class StreamDescriptionMock implements StreamDescription {
        private StreamType mType;

        private StreamDescriptionMock(final StreamType type) {
            mType = type;
        }

        @Override
        public StreamType getType() {
            return mType;
        }

        @Override
        public StreamMode getMode() {
            return null;
        }

        @Override
        public String getUfrag() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public List<RtcCandidate> getCandidates() {
            return null;
        }

        @Override
        public String getDtlsSetup() {
            return null;
        }

        @Override
        public String getFingerprint() {
            return null;
        }

        @Override
        public String getFingerprintHashFunction() {
            return null;
        }

        @Override
        public String getMediaStreamId() {
            return null;
        }

        @Override
        public String getMediaStreamTrackId() {
            return null;
        }

        @Override
        public String getCname() {
            return null;
        }

        @Override
        public boolean isRtcpMux() {
            return false;
        }

        @Override
        public List<Long> getSsrcs() {
            return null;
        }

        @Override
        public List<RtcPayload> getPayloads() {
            return null;
        }

        @Override
        public int getSctpPort() {
            return 0;
        }

        @Override
        public int getMaxMessageSize() {
            return 0;
        }

        @Override
        public String getAppLabel() {
            return null;
        }
    }

    private class StreamMock implements StreamSet.Stream {
        private StreamMode mStreamMode;
        private StreamType mType;

        private StreamMock(final StreamType type) {
            mType = type;
        }

        public StreamMode getStreamMode() {
            return mStreamMode;
        }

        @Override
        public StreamType getType() {
            return mType;
        }

        @Override
        public void setStreamMode(final StreamMode mode) {
            mStreamMode = mode;
        }
    }
}
