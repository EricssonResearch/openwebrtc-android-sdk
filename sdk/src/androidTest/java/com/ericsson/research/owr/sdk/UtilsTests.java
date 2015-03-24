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

        List<RtcPayload> intersection3 = Utils.intersectPayloads(sValidPayloads3, sDefaultVideoPayloads);
        assertEquals(1, intersection3.size());
        assertEquals(false, intersection3.get(0).isNack());
        assertEquals(false, intersection3.get(0).isNackPli());
        assertEquals(true, intersection3.get(0).isCcmFir());

        List<RtcPayload> intersection4 = Utils.intersectPayloads(sValidPayloads4, sDefaultVideoPayloads);
        assertEquals(1, intersection4.size());
        assertEquals(false, intersection4.get(0).isNack());
        assertEquals(true, intersection4.get(0).isNackPli());
        assertEquals(false, intersection4.get(0).isCcmFir());

        List<RtcPayload> intersection5 = Utils.intersectPayloads(sValidPayloads5, sDefaultVideoPayloads);
        assertEquals(1, intersection5.size());
        assertEquals(false, intersection5.get(0).isNack());
        assertEquals(false, intersection5.get(0).isNackPli());
        assertEquals(false, intersection5.get(0).isCcmFir());
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
}
