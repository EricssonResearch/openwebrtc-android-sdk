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

import com.ericsson.research.owr.Owr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class SessionDescriptionsTest extends AndroidTestCase {

    static {
        Owr.init();
    }

    private static final String sSimpleOffer = "{\"sdp\":\"v=0\r\n" +
            "o=- 1426854267315236600 1 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "\",\"type\":\"offer\"}";

    private static final String sSimpleAnswer = "{\"sdp\":\"v=0\r\n" +
            "o=- 1426854267315236600 1 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "\",\"type\":\"answer\"}";

    private static final String sInvalidType = "{\"sdp\":\"v=0\r\n" +
            "o=- 1426854267315236600 1 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "\",\"type\":\"answ3r\"}";

    private static final String sMissingType = "{\"sdp\":\"v=0\r\n" +
            "o=- 1426854267315236600 1 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "\",\"typ3\":\"answer\"}";

    private static final String sMissingSdp = "{\"5dp\":\"v=0\r\n" +
            "o=- 1426854267315236600 1 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "\",\"type\":\"answer\"}";

    private static final String sInvalidSdp = "{\"sdp\":\"nope\",\"type\":\"answer\"}";

    private static final String sChromeOffer = "{\"sdp\":\"v=0\\r\\n" +
            "o=- 7407423127539558064 2 IN IP4 127.0.0.1\\r\\n" +
            "s=-\\r\\n" +
            "t=0 0\\r\\n" +
            "a=group:BUNDLE audio video\\r\\n" +
            "a=msid-semantic: WMS KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3\\r\\n" +
            "m=audio 50238 RTP/SAVPF 111 103 104 9 0 8 106 105 13 126\\r\\n" +
            "c=IN IP4 129.192.20.149\\r\\n" +
            "a=rtcp:50238 IN IP4 129.192.20.149\\r\\n" +
            "a=candidate:4000241536 1 udp 2122260223 129.192.20.149 50238 typ host generation 0\\r\\n" +
            "a=candidate:4000241536 2 udp 2122260223 129.192.20.149 50238 typ host generation 0\\r\\n" +
            "a=candidate:3039919289 1 udp 2122194687 147.214.153.229 49997 typ host generation 0\\r\\n" +
            "a=candidate:3039919289 2 udp 2122194687 147.214.153.229 49997 typ host generation 0\\r\\n" +
            "a=ice-ufrag:eNiJLDtGPTtx8J8b\\r\\n" +
            "a=ice-pwd:7f1lY4bUNTcw/DxFk4a0LY3j\\r\\n" +
            "a=ice-options:google-ice\\r\\n" +
            "a=fingerprint:sha-256 A8:B1:8B:70:89:B2:56:10:98:87:4F:A2:4A:0D:FE:76:C6:5C:8D:9D:4F:0B:12:4C:DB:B0:FC:08:8E:FF:B5:43\\r\\n" +
            "a=setup:actpass\\r\\n" +
            "a=mid:audio\\r\\n" +
            "a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\\r\\n" +
            "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\\r\\n" +
            "a=sendrecv\\r\\n" +
            "a=rtcp-mux\\r\\n" +
            "a=rtpmap:111 opus/48000/2\\r\\n" +
            "a=fmtp:111 minptime=10; useinbandfec=1\\r\\n" +
            "a=rtpmap:103 ISAC/16000\\r\\n" +
            "a=rtpmap:104 ISAC/32000\\r\\n" +
            "a=rtpmap:9 G722/8000\\r\\n" +
            "a=rtpmap:0 PCMU/8000\\r\\n" +
            "a=rtpmap:8 PCMA/8000\\r\\n" +
            "a=rtpmap:106 CN/32000\\r\\n" +
            "a=rtpmap:105 CN/16000\\r\\n" +
            "a=rtpmap:13 CN/8000\\r\\n" +
            "a=rtpmap:126 telephone-event/8000\\r\\n" +
            "a=maxptime:60\\r\\n" +
            "a=ssrc:669595811 cname:pYPXnl/14H8HkFDP\\r\\n" +
            "a=ssrc:669595811 msid:KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3 0afb7ebc-24ae-489b-95af-8fc45217e81e\\r\\n" +
            "a=ssrc:669595811 mslabel:KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3\\r\\n" +
            "a=ssrc:669595811 label:0afb7ebc-24ae-489b-95af-8fc45217e81e\\r\\n" +
            "m=video 50238 RTP/SAVPF 100 116 117 96\\r\\n" +
            "c=IN IP4 129.192.20.149\\r\\n" +
            "a=rtcp:50238 IN IP4 129.192.20.149\\r\\n" +
            "a=candidate:4000241536 1 udp 2122260223 129.192.20.149 50238 typ host generation 0\\r\\n" +
            "a=candidate:4000241536 2 udp 2122260223 129.192.20.149 50238 typ host generation 0\\r\\n" +
            "a=candidate:3039919289 1 udp 2122194687 147.214.153.229 49997 typ host generation 0\\r\\n" +
            "a=candidate:3039919289 2 udp 2122194687 147.214.153.229 49997 typ host generation 0\\r\\n" +
            "a=ice-ufrag:eNiJLDtGPTtx8J8b\\r\\n" +
            "a=ice-pwd:7f1lY4bUNTcw/DxFk4a0LY3j\\r\\n" +
            "a=ice-options:google-ice\\r\\n" +
            "a=fingerprint:sha-256 A8:B1:8B:70:89:B2:56:10:98:87:4F:A2:4A:0D:FE:76:C6:5C:8D:9D:4F:0B:12:4C:DB:B0:FC:08:8E:FF:B5:43\\r\\n" +
            "a=setup:actpass\\r\\n" +
            "a=mid:video\\r\\n" +
            "a=extmap:2 urn:ietf:params:rtp-hdrext:toffset\\r\\n" +
            "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\\r\\n" +
            "a=sendrecv\\r\\n" +
            "a=rtcp-mux\\r\\n" +
            "a=rtpmap:100 VP8/90000\\r\\n" +
            "a=rtcp-fb:100 ccm fir\\r\\n" +
            "a=rtcp-fb:100 nack\\r\\n" +
            "a=rtcp-fb:100 nack pli\\r\\n" +
            "a=rtcp-fb:100 goog-remb\\r\\n" +
            "a=rtpmap:116 red/90000\\r\\n" +
            "a=rtpmap:117 ulpfec/90000\\r\\n" +
            "a=rtpmap:96 rtx/90000\\r\\n" +
            "a=fmtp:96 apt=100\\r\\n" +
            "a=ssrc-group:FID 3815039976 1660953852\\r\\n" +
            "a=ssrc:3815039976 cname:pYPXnl/14H8HkFDP\\r\\n" +
            "a=ssrc:3815039976 msid:KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3 304afecb-f5fd-42a1-b9d3-80b61e28794\\r\\n" +
            "a=ssrc:3815039976 mslabel:KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3\\r\\n" +
            "a=ssrc:3815039976 label:304afecb-f5fd-42a1-b9d3-080b61e28794\\r\\n" +
            "a=ssrc:1660953852 cname:pYPXnl/14H8HkFDP\\r\\n" +
            "a=ssrc:1660953852 msid:KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3 304afecb-f5fd-42a1-b9d3-80b61e28794\\r\\n" +
            "a=ssrc:1660953852 mslabel:KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3\\r\\n" +
            "a=ssrc:1660953852 label:304afecb-f5fd-42a1-b9d3-080b61e28794\\r\\n" +
            "\",\"type\":\"offer\"}";


    private static final String sOwrAnswer = "{\"sdp\":\"v=0\\r\\n" +
            "o=- 1426847364578889500 1 IN IP4 127.0.0.1\\r\\n" +
            "s=-\\r\\n" +
            "t=0 0\\r\\n" +
            "m=audio 1 RTP/SAVPF 111\\r\\n" +
            "c=IN IP4 0.0.0.0\\r\\n" +
            "a=rtcp-mux\\r\\n" +
            "a=sendrecv\\r\\n" +
            "a=rtpmap:111 opus/48000/2\\r\\n" +
            "a=ice-ufrag:lE0E\\r\\n" +
            "a=ice-pwd:4t1Unjq7+wO725A/GfR3+e\\r\\n" +
            "a=candidate:1 1 UDP 2013266431 fe80::b49a:e5c9:46f0:fd8b 57119 typ host\\r\\n" +
            "a=candidate:2 1 TCP 1019216383 fe80::b49a:e5c9:46f0:fd8b 0 typ host tcptype active\\r\\n" +
            "a=candidate:3 1 TCP 1015022079 fe80::b49a:e5c9:46f0:fd8b 60117 typ host tcptype passive\\r\\n" +
            "a=candidate:4 1 UDP 2013266431 fe80::4c98:ddff:fef6:dfe1 57120 typ host\\r\\n" +
            "a=candidate:5 1 TCP 1019216383 fe80::4c98:ddff:fef6:dfe1 0 typ host tcptype active\\r\\n" +
            "a=candidate:6 1 TCP 1015022079 fe80::4c98:ddff:fef6:dfe1 60119 typ host tcptype passive\\r\\n" +
            "a=candidate:7 1 UDP 2013266431 129.192.20.180 54404 typ host\\r\\n" +
            "a=candidate:8 1 TCP 1019216383 129.192.20.180 0 typ host tcptype active\\r\\n" +
            "a=candidate:9 1 TCP 1015022079 129.192.20.180 60121 typ host tcptype passive\\r\\n" +
            "a=candidate:10 1 UDP 2013266431 fe80::464:65eb:5790:23fe 54405 typ host\\r\\n" +
            "a=candidate:11 1 TCP 1019216383 fe80::464:65eb:5790:23fe 0 typ host tcptype active\\r\\n" +
            "a=candidate:12 1 TCP 1015022079 fe80::464:65eb:5790:23fe 60123 typ host tcptype passive\\r\\n" +
            "a=candidate:13 1 UDP 2013266431 95.192.252.56 58297 typ host\\r\\n" +
            "a=candidate:14 1 TCP 1019216383 95.192.252.56 0 typ host tcptype active\\r\\n" +
            "a=candidate:15 1 TCP 1015022079 95.192.252.56 60125 typ host tcptype passive\\r\\n" +
            "a=candidate:31 1 TCP 847249919 129.192.20.180 0 typ srflx raddr fe80::b49a:e5c9:46f0:fd8b rport 0 tcptype active\\r\\n" +
            "a=candidate:32 1 TCP 843055615 129.192.20.180 60117 typ srflx raddr fe80::b49a:e5c9:46f0:fd8b rport 60117 tcptype passive\\r\\n" +
            "a=candidate:33 1 UDP 167772671 129.192.20.82 64537 typ relay raddr fe80::b49a:e5c9:46f0:fd8b rport 0\\r\\n" +
            "a=candidate:36 1 UDP 167772671 129.192.20.82 59008 typ relay raddr 129.192.20.180 rport 0\\r\\n" +
            "a=candidate:37 1 TCP 847249919 129.192.20.180 0 typ srflx raddr fe80::4c98:ddff:fef6:dfe1 rport 0 tcptype active\\r\\n" +
            "a=candidate:38 1 TCP 843055615 129.192.20.180 60119 typ srflx raddr fe80::4c98:ddff:fef6:dfe1 rport 60119 tcptype passive\\r\\n" +
            "a=candidate:39 1 UDP 167772671 129.192.20.82 63967 typ relay raddr fe80::4c98:ddff:fef6:dfe1 rport 0\\r\\n" +
            "a=candidate:43 1 TCP 847249919 129.192.20.180 0 typ srflx raddr 95.192.252.56 rport 0 tcptype active\\r\\n" +
            "a=candidate:44 1 TCP 843055615 129.192.20.180 60125 typ srflx raddr 95.192.252.56 rport 60125 tcptype passive\\r\\n" +
            "a=candidate:45 1 UDP 167772671 129.192.20.82 57347 typ relay raddr 95.192.252.56 rport 0\\r\\n" +
            "a=candidate:49 1 TCP 847249919 129.192.20.180 0 typ srflx raddr fe80::464:65eb:5790:23fe rport 0 tcptype active\\r\\n" +
            "a=candidate:50 1 TCP 843055615 129.192.20.180 60123 typ srflx raddr fe80::464:65eb:5790:23fe rport 60123 tcptype passive\\r\\n" +
            "a=candidate:51 1 UDP 167772671 129.192.20.82 55778 typ relay raddr fe80::464:65eb:5790:23fe rport 0\\r\\n" +
            "a=fingerprint:sha-256 8A:48:DC:0C:1F:0A:43:D0:DF:0C:00:3F:64:73:52:2B:DB:5B:43:D1:7A:C7:CA:0D:2C:C4:4E:29:6E:14:98:06\\r\\n" +
            "a=setup:active\\r\\n" +
            "m=video 1 RTP/SAVPF 100\\r\\n" +
            "c=IN IP4 0.0.0.0\\r\\n" +
            "a=rtcp-mux\\r\\n" +
            "a=sendrecv\\r\\n" +
            "a=rtpmap:100 VP8/90000\\r\\n" +
            "a=rtcp-fb:100 nack pli\\r\\n" +
            "a=rtcp-fb:100 ccm fir\\r\\n" +
            "a=ice-ufrag:6g7y\\r\\n" +
            "a=ice-pwd:oxwRMd4ze31bkn7RwQtOn8\\r\\n" +
            "a=candidate:16 1 UDP 2013266431 fe80::b49a:e5c9:46f0:fd8b 58298 typ host\\r\\n" +
            "a=candidate:17 1 TCP 1019216383 fe80::b49a:e5c9:46f0:fd8b 0 typ host tcptype active\\r\\n" +
            "a=candidate:18 1 TCP 1015022079 fe80::b49a:e5c9:46f0:fd8b 60127 typ host tcptype passive\\r\\n" +
            "a=candidate:19 1 UDP 2013266431 fe80::4c98:ddff:fef6:dfe1 58299 typ host\\r\\n" +
            "a=candidate:20 1 TCP 1019216383 fe80::4c98:ddff:fef6:dfe1 0 typ host tcptype active\\r\\n" +
            "a=candidate:21 1 TCP 1015022079 fe80::4c98:ddff:fef6:dfe1 60129 typ host tcptype passive\\r\\n" +
            "a=candidate:22 1 UDP 2013266431 129.192.20.180 54211 typ host\\r\\n" +
            "a=candidate:23 1 TCP 1019216383 129.192.20.180 0 typ host tcptype active\\r\\n" +
            "a=candidate:24 1 TCP 1015022079 129.192.20.180 60131 typ host tcptype passive\\r\\n" +
            "a=candidate:25 1 UDP 2013266431 fe80::464:65eb:5790:23fe 54212 typ host\\r\\n" +
            "a=candidate:26 1 TCP 1019216383 fe80::464:65eb:5790:23fe 0 typ host tcptype active\\r\\n" +
            "a=candidate:27 1 TCP 1015022079 fe80::464:65eb:5790:23fe 60133 typ host tcptype passive\\r\\n" +
            "a=candidate:28 1 UDP 2013266431 95.192.252.56 62583 typ host\\r\\n" +
            "a=candidate:29 1 TCP 1019216383 95.192.252.56 0 typ host tcptype active\\r\\n" +
            "a=candidate:30 1 TCP 1015022079 95.192.252.56 60135 typ host tcptype passive\\r\\n" +
            "a=candidate:40 1 TCP 847249919 129.192.20.180 0 typ srflx raddr fe80::b49a:e5c9:46f0:fd8b rport 0 tcptype active\\r\\n" +
            "a=candidate:41 1 TCP 843055615 129.192.20.180 60127 typ srflx raddr fe80::b49a:e5c9:46f0:fd8b rport 60127 tcptype passive\\r\\n" +
            "a=candidate:42 1 UDP 167772671 129.192.20.82 61595 typ relay raddr fe80::b49a:e5c9:46f0:fd8b rport 0\\r\\n" +
            "a=candidate:46 1 TCP 847249919 129.192.20.180 0 typ srflx raddr fe80::4c98:ddff:fef6:dfe1 rport 0 tcptype active\\r\\n" +
            "a=candidate:47 1 TCP 843055615 129.192.20.180 60129 typ srflx raddr fe80::4c98:ddff:fef6:dfe1 rport 60129 tcptype passive\\r\\n" +
            "a=candidate:48 1 UDP 167772671 129.192.20.82 54409 typ relay raddr fe80::4c98:ddff:fef6:dfe1 rport 0\\r\\n" +
            "a=candidate:54 1 UDP 167772671 129.192.20.82 63291 typ relay raddr 129.192.20.180 rport 0\\r\\n" +
            "a=candidate:55 1 TCP 847249919 129.192.20.180 0 typ srflx raddr fe80::464:65eb:5790:23fe rport 0 tcptype active\\r\\n" +
            "a=candidate:56 1 TCP 843055615 129.192.20.180 60133 typ srflx raddr fe80::464:65eb:5790:23fe rport 60133 tcptype passive\\r\\n" +
            "a=candidate:57 1 UDP 167772671 129.192.20.82 62315 typ relay raddr fe80::464:65eb:5790:23fe rport 0\\r\\n" +
            "a=candidate:58 1 TCP 847249919 129.192.20.180 0 typ srflx raddr 95.192.252.56 rport 0 tcptype active\\r\\n" +
            "a=candidate:59 1 TCP 843055615 129.192.20.180 60135 typ srflx raddr 95.192.252.56 rport 60135 tcptype passive\\r\\n" +
            "a=candidate:60 1 UDP 167772671 129.192.20.82 65418 typ relay raddr 95.192.252.56 rport 0\\r\\n" +
            "a=fingerprint:sha-256 8A:48:DC:0C:1F:0A:43:D0:DF:0C:00:3F:64:73:52:2B:DB:5B:43:D1:7A:C7:CA:0D:2C:C4:4E:29:6E:14:98:06\\r\\n" +
            "a=setup:active\\r\\n" +
            "\",\"type\":\"answer\"}";

    public void testSessionDescription() throws JSONException, InvalidDescriptionException {
        RtcCandidate candidate = new PlainRtcCandidate("4375",
                RtcCandidate.ComponentType.RTP, RtcCandidate.TransportType.UDP,
                23746, "12.34.56.78", 12345, RtcCandidate.CandidateType.HOST, null, -1);
        RtcPayload payload1 = new PlainRtcPayload(101, "lolcode", 1337,
                new HashMap<String, Object>(){{put("speed", "full");}}, 5, false, false, false);
        RtcPayload payload2 = new PlainRtcPayload(102, "wpm8", 1337,
                new HashMap<String, Object>(){{put("speed", "full");}}, -2, true, true, true);
        StreamDescription stream1 = new StreamDescriptionImpl(StreamType.AUDIO,
                StreamMode.RECEIVE_ONLY, "admin", "qweasd",
                Arrays.asList(candidate), "actpass", "12:23:34:45:56:67:78:89:90", "sha-256",
                "userycnfseuirymc", "mlsieudhsleiurm", "assdfg", true,
                Arrays.asList(837456324L), Arrays.asList(payload1));
        StreamDescription stream2 = new StreamDescriptionImpl(StreamType.VIDEO,
                StreamMode.SEND_ONLY, "admin", "qweasd",
                Arrays.asList(candidate), "actpass", "12:23:34:45:56:67:78:89:90", "sha-256",
                "userycnfseuirymc", "mlsieudhsleiurm", "assdfg", true,
                Arrays.asList(837456324L), Arrays.asList(payload2));
        StreamDescription stream3 = new StreamDescriptionImpl(StreamType.DATA,
                StreamMode.SEND_RECEIVE, "admin", "qweasd",
                Arrays.asList(candidate), "actpass", "12:23:34:45:56:67:78:89:90", "sha-256",
                5000, 1024, "webrtc-datachannel");
        SessionDescription sessionDescription = new SessionDescriptionImpl(
                SessionDescription.Type.OFFER, "123456789", Arrays.asList(stream1, stream2, stream3));
        JSONObject jsep = SessionDescriptions.toJsep(sessionDescription);
        assertNotNull(jsep);
        assertEquals("offer", jsep.getString("type"));
        String sdp = jsep.getString("sdp");
        assertNotNull(sdp);
        JSONObject sdpJson = SdpProcessor.sdpToJson(sdp);
        assertNotNull(sdpJson);
        assertEquals("123456789", sdpJson.getJSONObject("originator").getString("sessionId"));
        JSONArray mediaDescriptions = sdpJson.getJSONArray("mediaDescriptions");
        assertEquals(3, mediaDescriptions.length());
        JSONObject mediaDesc1 = mediaDescriptions.getJSONObject(0);
        JSONObject mediaDesc2 = mediaDescriptions.getJSONObject(1);
        JSONObject mediaDesc3 = mediaDescriptions.getJSONObject(2);
        assertEquals("audio", mediaDesc1.getString("type"));
        assertEquals("video", mediaDesc2.getString("type"));
        assertEquals("application", mediaDesc3.getString("type"));
        assertNull(mediaDesc1.optString("mid", null));
        assertNull(mediaDesc2.optString("mid", null));
        assertNull(mediaDesc3.optString("mid", null));
        assertEquals("recvonly", mediaDesc1.getString("mode"));
        assertEquals("sendonly", mediaDesc2.getString("mode"));
        assertEquals("sendrecv", mediaDesc3.getString("mode"));
        assertEquals("admin", mediaDesc1.getJSONObject("ice").getString("ufrag"));
        assertEquals("admin", mediaDesc2.getJSONObject("ice").getString("ufrag"));
        assertEquals("admin", mediaDesc3.getJSONObject("ice").getString("ufrag"));
        assertEquals("qweasd", mediaDesc1.getJSONObject("ice").getString("password"));
        assertEquals("qweasd", mediaDesc2.getJSONObject("ice").getString("password"));
        assertEquals("qweasd", mediaDesc3.getJSONObject("ice").getString("password"));
        assertEquals("12:23:34:45:56:67:78:89:90", mediaDesc1.getJSONObject("dtls").getString("fingerprint"));
        assertEquals("12:23:34:45:56:67:78:89:90", mediaDesc2.getJSONObject("dtls").getString("fingerprint"));
        assertEquals("12:23:34:45:56:67:78:89:90", mediaDesc3.getJSONObject("dtls").getString("fingerprint"));
        assertEquals("sha-256", mediaDesc1.getJSONObject("dtls").getString("fingerprintHashFunction"));
        assertEquals("sha-256", mediaDesc2.getJSONObject("dtls").getString("fingerprintHashFunction"));
        assertEquals("sha-256", mediaDesc3.getJSONObject("dtls").getString("fingerprintHashFunction"));
        assertEquals("actpass", mediaDesc1.getJSONObject("dtls").getString("setup"));
        assertEquals("actpass", mediaDesc2.getJSONObject("dtls").getString("setup"));
        assertEquals("actpass", mediaDesc3.getJSONObject("dtls").getString("setup"));
        assertEquals(1, mediaDesc1.getJSONObject("ice").getJSONArray("candidates").length());
        assertEquals(1, mediaDesc2.getJSONObject("ice").getJSONArray("candidates").length());
        assertEquals(1, mediaDesc3.getJSONObject("ice").getJSONArray("candidates").length());
        JSONObject candidate1 = mediaDesc1.getJSONObject("ice").getJSONArray("candidates").getJSONObject(0);
        JSONObject candidate2 = mediaDesc2.getJSONObject("ice").getJSONArray("candidates").getJSONObject(0);
        JSONObject candidate3 = mediaDesc3.getJSONObject("ice").getJSONArray("candidates").getJSONObject(0);
        assertEquals("4375", candidate1.getString("foundation"));
        assertEquals("4375", candidate2.getString("foundation"));
        assertEquals("4375", candidate3.getString("foundation"));
        assertEquals("UDP", candidate1.getString("transport"));
        assertEquals("UDP", candidate2.getString("transport"));
        assertEquals("UDP", candidate3.getString("transport"));
        assertEquals("host", candidate1.getString("type"));
        assertEquals("host", candidate2.getString("type"));
        assertEquals("host", candidate3.getString("type"));
        assertEquals(1, candidate1.getInt("componentId"));
        assertEquals(1, candidate2.getInt("componentId"));
        assertEquals(1, candidate3.getInt("componentId"));
        assertEquals("12.34.56.78", candidate1.getString("address"));
        assertEquals("12.34.56.78", candidate2.getString("address"));
        assertEquals("12.34.56.78", candidate3.getString("address"));
        assertEquals(12345, candidate1.getInt("port"));
        assertEquals(12345, candidate2.getInt("port"));
        assertEquals(12345, candidate3.getInt("port"));
        assertEquals(23746, candidate1.getInt("priority"));
        assertEquals(23746, candidate2.getInt("priority"));
        assertEquals(23746, candidate3.getInt("priority"));
        JSONObject jsonPayload1 = mediaDesc1.getJSONArray("payloads").getJSONObject(0);
        JSONObject jsonPayload2 = mediaDesc2.getJSONArray("payloads").getJSONObject(0);
        assertEquals(101, jsonPayload1.getInt("type"));
        assertEquals(102, jsonPayload2.getInt("type"));
        assertEquals("lolcode", jsonPayload1.getString("encodingName"));
        assertEquals("wpm8", jsonPayload2.getString("encodingName"));
        assertEquals(1337, jsonPayload1.getInt("clockRate"));
        assertEquals(1337, jsonPayload2.getInt("clockRate"));
        assertEquals("full", jsonPayload1.getJSONObject("parameters").getString("speed"));
        assertEquals("full", jsonPayload2.getJSONObject("parameters").getString("speed"));
        assertEquals(5, jsonPayload1.getInt("channels"));
        assertFalse(jsonPayload1.has("nack"));
        assertFalse(jsonPayload1.has("nackpli"));
        assertFalse(jsonPayload1.has("ccmfir"));
        assertFalse(jsonPayload2.has("channels"));
        assertTrue(jsonPayload2.getBoolean("nack"));
        assertTrue(jsonPayload2.getBoolean("nackpli"));
        assertTrue(jsonPayload2.getBoolean("ccmfir"));
        assertEquals("webrtc-datachannel", mediaDesc3.getJSONObject("sctp").getString("app"));
        assertEquals(5000, mediaDesc3.getJSONObject("sctp").getInt("port"));
        assertEquals(1024, mediaDesc3.getJSONObject("sctp").getInt("streams"));

        SessionDescription sameDesc = SessionDescriptions.fromJsep(jsep);
        assertNotNull(sameDesc);
    }

    public void testSimpleOffer() throws JSONException, InvalidDescriptionException {
        JSONObject simpleOffer = new JSONObject(sSimpleOffer);
        SessionDescription offer = SessionDescriptions.fromJsep(simpleOffer);
        assertNotNull(offer);
        assertEquals(SessionDescription.Type.OFFER, offer.getType());
        assertEquals("1426854267315236600", offer.getSessionId());
        assertTrue(offer.getStreamDescriptions().isEmpty());
        assertFalse(offer.hasStreamType(StreamType.AUDIO));
        assertFalse(offer.hasStreamType(StreamType.VIDEO));
        assertFalse(offer.hasStreamType(StreamType.DATA));
        JSONObject jsep = SessionDescriptions.toJsep(offer);
        assertNotNull(jsep);
        assertEquals("offer", jsep.getString("type"));
        String sdp = jsep.getString("sdp");
        assertNotNull(sdp);
        JSONObject sdpJson = SdpProcessor.sdpToJson(sdp);
        assertNotNull(sdpJson);
        assertEquals("1426854267315236600", sdpJson.getJSONObject("originator").getString("sessionId"));
    }

    public void testSimpleAnswer() throws JSONException, InvalidDescriptionException {
        JSONObject simpleAnswer = new JSONObject(sSimpleAnswer);
        SessionDescription answer = SessionDescriptions.fromJsep(simpleAnswer);
        assertNotNull(answer);
        assertEquals(SessionDescription.Type.ANSWER, answer.getType());
        assertEquals("1426854267315236600", answer.getSessionId());
        assertTrue(answer.getStreamDescriptions().isEmpty());
        assertFalse(answer.hasStreamType(StreamType.AUDIO));
        assertFalse(answer.hasStreamType(StreamType.VIDEO));
        assertFalse(answer.hasStreamType(StreamType.DATA));
        JSONObject jsep = SessionDescriptions.toJsep(answer);
        assertNotNull(jsep);
        assertEquals("answer", jsep.getString("type"));
        String sdp = jsep.getString("sdp");
        assertNotNull(sdp);
        JSONObject sdpJson = SdpProcessor.sdpToJson(sdp);
        assertNotNull(sdpJson);
        assertEquals("1426854267315236600", sdpJson.getJSONObject("originator").getString("sessionId"));
    }

    public void testChromeOffer() throws JSONException, InvalidDescriptionException {
        JSONObject json = new JSONObject(sChromeOffer);
        SessionDescription desc = SessionDescriptions.fromJsep(json);
        assertNotNull(desc);
        assertEquals("7407423127539558064", desc.getSessionId());
        assertEquals(SessionDescription.Type.OFFER, desc.getType());
        assertEquals(2, desc.getStreamDescriptions().size());

        StreamDescription audio = desc.getStreamDescriptions().get(0);

        assertEquals(-1, audio.getSctpPort());
        assertNull(audio.getAppLabel());

        assertNotNull(audio.getCname());
        assertEquals("actpass", audio.getDtlsSetup());
        assertEquals("sha-256", audio.getFingerprintHashFunction());
        String fingerprint = "A8:B1:8B:70:89:B2:56:10:98:87:4F:A2:4A:0D:FE:76:C6:5C:8D:9D:4F:0B:12:4C:DB:B0:FC:08:8E:FF:B5:43";
        assertEquals(fingerprint, audio.getFingerprint());
        assertEquals("KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3", audio.getMediaStreamId());
        assertEquals(4, audio.getCandidates().size());
        assertEquals("4000241536", audio.getCandidates().get(0).getFoundation());
        assertEquals(RtcCandidate.ComponentType.RTP, audio.getCandidates().get(0).getComponentType());
        assertEquals(RtcCandidate.TransportType.UDP, audio.getCandidates().get(0).getTransportType());
        assertEquals(2122260223, audio.getCandidates().get(0).getPriority());
        assertEquals("129.192.20.149", audio.getCandidates().get(0).getAddress());
        assertEquals(50238, audio.getCandidates().get(0).getPort());
        assertEquals(RtcCandidate.CandidateType.HOST, audio.getCandidates().get(0).getType());
        assertEquals("eNiJLDtGPTtx8J8b", audio.getCandidates().get(0).getUfrag());
        assertEquals("7f1lY4bUNTcw/DxFk4a0LY3j", audio.getCandidates().get(0).getPassword());
        assertEquals(StreamMode.SEND_RECEIVE, audio.getMode());
        assertEquals("eNiJLDtGPTtx8J8b", audio.getUfrag());
        assertEquals("7f1lY4bUNTcw/DxFk4a0LY3j", audio.getPassword());
        assertEquals(10, audio.getPayloads().size());
        assertEquals("opus", audio.getPayloads().get(0).getEncodingName());
        assertEquals(111, audio.getPayloads().get(0).getPayloadType());
        assertEquals(48000, audio.getPayloads().get(0).getClockRate());
        assertEquals(2, audio.getPayloads().get(0).getChannels());
        assertEquals(10, audio.getPayloads().get(0).getParameters().get("minptime"));
        // the parameter isn't parsed properly
        assertNull(audio.getPayloads().get(0).getParameters().get("useinbandfec"));
    }

    public void testInvalidType() throws JSONException {
        JSONObject json = new JSONObject(sInvalidType);
        assertNotNull(json);
        try {
            SessionDescriptions.fromJsep(json);
        } catch (InvalidDescriptionException e) {
            assertNull(e.getCause());
            return;
        }
        throw new RuntimeException("should not be reached");
    }

    public void testMissingType() throws JSONException {
        JSONObject json = new JSONObject(sMissingType);
        assertNotNull(json);
        try {
            SessionDescriptions.fromJsep(json);
        } catch (InvalidDescriptionException e) {
            assertTrue(e.getCause() instanceof JSONException);
            return;
        }
        throw new RuntimeException("should not be reached");
    }

    public void testMissingSdp() throws JSONException {
        JSONObject json = new JSONObject(sMissingSdp);
        assertNotNull(json);
        try {
            SessionDescriptions.fromJsep(json);
        } catch (InvalidDescriptionException e) {
            assertTrue(e.getCause() instanceof JSONException);
            return;
        }
        throw new RuntimeException("should not be reached");
    }

    public void testInvalidSd() throws JSONException {
        JSONObject json = new JSONObject(sInvalidSdp);
        assertNotNull(json);
        try {
            SessionDescriptions.fromJsep(json);
        } catch (InvalidDescriptionException e) {
            assertTrue(e.getCause() instanceof JSONException);
            return;
        }
        throw new RuntimeException("should not be reached");
    }
}
