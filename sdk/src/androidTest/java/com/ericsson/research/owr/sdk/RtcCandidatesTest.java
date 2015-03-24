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

import com.ericsson.research.owr.Candidate;
import com.ericsson.research.owr.CandidateType;
import com.ericsson.research.owr.ComponentType;
import com.ericsson.research.owr.TransportType;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

public class RtcCandidatesTest extends TestCase {
    public static final String TAG = "RtcCandidatesTest";

    private static final String[] validAttributeLines = new String[] {
            "candidate:0 1 UDP 2130379007 172.20.10.2 51850 typ host\r\n",
            "candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host",
            "a=candidate:2 1 UDP 2130379007 172.20.10.2 51850 typ host\r\n",
            "a=candidate:3 2 UDP 2130379006 172.20.10.2 61466 typ host",
            "candidate:4 1 UDP 1694236671 90.237.24.157 35856 typ srflx raddr 172.20.10.2 rport 51850\r\n",
            "candidate:5 2 UDP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 rport 61466",
            "a=candidate:6 1 UDP 1694236671 90.237.24.157 35856 typ srflx raddr 172.20.10.2 rport 51850\r\n",
            "a=candidate:7 2 UDP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 rport 61466",
            "candidate:8 1 UDP 100401151 192.36.158.14 55300 typ relay raddr 192.36.158.14 rport 55300",
            "candidate:9 2 UDP 100401150 192.36.158.14 61720 typ relay raddr 192.36.158.14 rport 61720\r\n",
            "a=candidate:10 1 UDP 100401151 192.36.158.14 55300 typ relay raddr 192.36.158.14 rport 55300",
            "a=candidate:11 2 UDP 100401150 192.36.158.14 61720 typ relay raddr 192.36.158.14 rport 61720\r\n"
    };

    private static final String[] invalidAttributeLines = new String[] {
            "andidate:0 1 UDP 2130379007 172.20.10.2 51850 typ host",
            "candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\r",
            "candidate:2 1 UDP 2130379007 172.20.10.2 51850 typ host\n",
            "b=candidate:3 2 UDP 2130379006 172.20.10.2 61466 typ host",
            "candidate:4 0 UDP 2130379007 172.20.10.2 51850 typ host",
            "candidate:5 1 UD 2130379007 172.20.10.2 51850 typ host",
            "candidate:6 1 UDP 2130379007 172.20.10.2 5 51850 typ host",
            "candidate:7 1 UDP 2130379007 172.20.10.2 51850 type host",
            "candidate:8 1 UDP 2130379007 172.20.10.2 51850 type hos",
            "candidate:9 1 UDP 1694236671 90.237.24.157 35856 typ srflx",
            "candidate:10 2 UDP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 5 rport 61466",
            "candidate:11 1 UDP 1694236671 90.237.24.157 35856 typ srflx raddr 172.20.10.2 rport",
            "candidate:12 2 UDP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2",
            "candidate:13 2 UDP 1694236670 90.237.24.157 32790 typ srflx rport 61466",
            "candidate:14 1 UDP 100401151 192.36.158.14 55300 typ relay raddr 172.20.10.2 5 rport 61466",
            "candidate:15 2 UDP 100401150 192.36.158.14 61720 typ relay raddr 172.20.10.2 rport",
            "candidate:16 1 UDP 100401151 192.36.158.14 55300 typ relay raddr 172.20.10.2",
            "candidate:17 2 UDP 100401150 192.36.158.14 61720 typ relay raddr 61466",
    };

    public void testAttributeConversion() {
        for (String line : validAttributeLines) {
            RtcCandidate candidate = RtcCandidates.fromSdpAttribute(line);
            assertNotNull(line, candidate);
            String newLine = RtcCandidates.toSdpAttribute(candidate);
            if (line.startsWith("a=")) {
                line = line.substring(2);
            }
            if (line.endsWith("\r\n")) {
                line = line.substring(0, line.length() - 2);
            }
            assertEquals(line, newLine);
        }
        for (String invalidLine : invalidAttributeLines) {
            RtcCandidate candidate = RtcCandidates.fromSdpAttribute(invalidLine);
            assertNull(invalidLine, candidate);
        }
    }


    private static String[] validJsepJsons = new String[] {
            "{\"sdpMid\":\"video\",\"sdpMLineIndex\":1,\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMid\":null,\"sdpMLineIndex\":1,\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMid\":\"video\",\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMid\":\"video\",\"sdpMLineIndex\":\"1\",\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMid\":\"video\",\"sdpMLineIndex\":\"asd\",\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMid\":\"video\",\"sdpMLineIndex\":null,\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMid\":\"video\",\"sdpMLineIndex\":false,\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
            "{\"sdpMLineIndex\":1,\"candidate\":\"candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host\"}",
    };

    public void testJsepConversion() throws JSONException {
        RtcCandidate[] candidates = new RtcCandidate[validJsepJsons.length];
        for (int i = 0; i < validJsepJsons.length; i++) {
            JSONObject jsep = new JSONObject(validJsepJsons[i]);
            assertNotNull(jsep);
            candidates[i] = RtcCandidates.fromJsep(jsep);
            assertNotNull(validJsepJsons[i], candidates[i]);
            assertEquals("candidate:1 2 UDP 2130379006 172.20.10.2 61466 typ host", RtcCandidates.toSdpAttribute(candidates[i]));
        }
        assertEquals("video", candidates[0].getStreamId());
        assertEquals(1, candidates[0].getStreamIndex());
        assertNull(candidates[1].getStreamId());
        assertEquals(1, candidates[1].getStreamIndex());
        assertEquals("video", candidates[2].getStreamId());
        assertEquals(-1, candidates[2].getStreamIndex());
        assertEquals("video", candidates[3].getStreamId());
        assertEquals(1, candidates[3].getStreamIndex());
        assertEquals("video", candidates[4].getStreamId());
        assertEquals(-1, candidates[4].getStreamIndex());
        assertEquals("video", candidates[5].getStreamId());
        assertEquals(-1, candidates[5].getStreamIndex());
        assertEquals("video", candidates[6].getStreamId());
        assertEquals(-1, candidates[6].getStreamIndex());
        assertNull(candidates[7].getStreamId());
        assertEquals(1, candidates[7].getStreamIndex());

        JSONObject[] backToJson = new JSONObject[validJsepJsons.length];

        for (int i = 0; i < validJsepJsons.length; i++) {
            backToJson[i] = RtcCandidates.toJsep(candidates[i]);
            assertNotNull(backToJson[i]);
        }

        assertEquals("video", backToJson[0].optString("sdpMid"));
        assertTrue(backToJson[1].isNull("sdpMid"));
        assertEquals("video", backToJson[2].optString("sdpMid"));
        assertEquals("video", backToJson[3].optString("sdpMid"));
        assertEquals("video", backToJson[4].optString("sdpMid"));
        assertEquals("video", backToJson[5].optString("sdpMid"));
        assertEquals("video", backToJson[6].optString("sdpMid"));
        assertTrue(backToJson[7].isNull("sdpMid"));
        assertEquals(1, backToJson[0].optInt("sdpMLineIndex"));
        assertEquals(1, backToJson[1].optInt("sdpMLineIndex"));
        assertEquals(-1, backToJson[2].optInt("sdpMLineIndex", -1));
        assertEquals(1, backToJson[3].optInt("sdpMLineIndex"));
        assertEquals(-1, backToJson[4].optInt("sdpMLineIndex", -1));
        assertEquals(-1, backToJson[5].optInt("sdpMLineIndex", -1));
        assertEquals(-1, backToJson[6].optInt("sdpMLineIndex", -1));
        assertEquals(1, backToJson[7].optInt("sdpMLineIndex"));
    }

    public void testFromOwrCandidate() {
        Candidate candidate = new Candidate(CandidateType.PEER_REFLEXIVE, ComponentType.RTP);
        candidate.setUfrag("asd");
        candidate.setPassword("123");
        candidate.setFoundation("0");
        candidate.setTransportType(TransportType.TCP_PASSIVE);
        candidate.setPriority(5);
        candidate.setAddress("1.2.3.4");
        candidate.setPort(1234);
        candidate.setBaseAddress("2.3.4.5");
        candidate.setBasePort(2345);

        RtcCandidate rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals("asd", rtcCandidate.getUfrag());
        assertEquals("123", rtcCandidate.getPassword());
        assertEquals("0", rtcCandidate.getFoundation());
        assertEquals(RtcCandidate.ComponentType.RTP, rtcCandidate.getComponentType());
        assertEquals(RtcCandidate.TransportType.TCP_PASSIVE, rtcCandidate.getTransportType());
        assertEquals(5, rtcCandidate.getPriority());
        assertEquals("1.2.3.4", rtcCandidate.getAddress());
        assertEquals(1234, rtcCandidate.getPort());
        assertEquals(RtcCandidate.CandidateType.PEER_REFLEXIVE, rtcCandidate.getType());
        assertEquals("2.3.4.5", rtcCandidate.getRelatedAddress());
        assertEquals(2345, rtcCandidate.getRelatedPort());

        candidate.setTransportType(TransportType.TCP_ACTIVE);
        rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals(RtcCandidate.TransportType.TCP_ACTIVE, rtcCandidate.getTransportType());

        candidate.setTransportType(TransportType.TCP_SO);
        rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals(RtcCandidate.TransportType.TCP_SO, rtcCandidate.getTransportType());

        candidate.setTransportType(TransportType.UDP);
        rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals(RtcCandidate.TransportType.UDP, rtcCandidate.getTransportType());

        candidate = new Candidate(CandidateType.HOST, ComponentType.RTCP);
        candidate.setUfrag("asd");
        candidate.setPassword("123");
        candidate.setFoundation("0");
        candidate.setTransportType(TransportType.UDP);
        candidate.setPriority(5);
        candidate.setAddress("1.2.3.4");
        candidate.setPort(1234);

        rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals("asd", rtcCandidate.getUfrag());
        assertEquals("123", rtcCandidate.getPassword());
        assertEquals("0", rtcCandidate.getFoundation());
        assertEquals(RtcCandidate.ComponentType.RTCP, rtcCandidate.getComponentType());
        assertEquals(RtcCandidate.TransportType.UDP, rtcCandidate.getTransportType());
        assertEquals(5, rtcCandidate.getPriority());
        assertEquals("1.2.3.4", rtcCandidate.getAddress());
        assertEquals(1234, rtcCandidate.getPort());
        assertEquals(RtcCandidate.CandidateType.HOST, rtcCandidate.getType());


        candidate = new Candidate(CandidateType.RELAY, ComponentType.RTP);
        candidate.setUfrag("asd");
        candidate.setPassword("123");
        candidate.setFoundation("0");
        candidate.setTransportType(TransportType.TCP_SO);
        candidate.setPriority(5);
        candidate.setAddress("1.2.3.4");
        candidate.setPort(1234);
        candidate.setBaseAddress("2.3.4.5");
        candidate.setBasePort(2345);

        rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals("asd", rtcCandidate.getUfrag());
        assertEquals("123", rtcCandidate.getPassword());
        assertEquals("0", rtcCandidate.getFoundation());
        assertEquals(RtcCandidate.ComponentType.RTP, rtcCandidate.getComponentType());
        assertEquals(RtcCandidate.TransportType.TCP_SO, rtcCandidate.getTransportType());
        assertEquals(5, rtcCandidate.getPriority());
        assertEquals("1.2.3.4", rtcCandidate.getAddress());
        assertEquals(1234, rtcCandidate.getPort());
        assertEquals(RtcCandidate.CandidateType.RELAY, rtcCandidate.getType());
        assertEquals("2.3.4.5", rtcCandidate.getRelatedAddress());
        assertEquals(2345, rtcCandidate.getRelatedPort());


        candidate = new Candidate(CandidateType.SERVER_REFLEXIVE, ComponentType.RTCP);
        candidate.setUfrag("asd");
        candidate.setPassword("123");
        candidate.setFoundation("0");
        candidate.setTransportType(TransportType.TCP_ACTIVE);
        candidate.setPriority(5);
        candidate.setAddress("1.2.3.4");
        candidate.setPort(1234);
        candidate.setBaseAddress("2.3.4.5");
        candidate.setBasePort(2345);

        rtcCandidate = PlainRtcCandidate.fromOwrCandidate(candidate);
        assertEquals("asd", rtcCandidate.getUfrag());
        assertEquals("123", rtcCandidate.getPassword());
        assertEquals("0", rtcCandidate.getFoundation());
        assertEquals(RtcCandidate.ComponentType.RTCP, rtcCandidate.getComponentType());
        assertEquals(RtcCandidate.TransportType.TCP_ACTIVE, rtcCandidate.getTransportType());
        assertEquals(5, rtcCandidate.getPriority());
        assertEquals("1.2.3.4", rtcCandidate.getAddress());
        assertEquals(1234, rtcCandidate.getPort());
        assertEquals(RtcCandidate.CandidateType.SERVER_REFLEXIVE, rtcCandidate.getType());
        assertEquals("2.3.4.5", rtcCandidate.getRelatedAddress());
        assertEquals(2345, rtcCandidate.getRelatedPort());
    }
}
