/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import android.test.AndroidTestCase;

import com.ericsson.research.owr.Owr;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionDescriptionsTest extends AndroidTestCase {
    private SdpProcessor mSdpProcessor;

    {
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSdpProcessor = SdpProcessor.fromAssets(getContext().getAssets());
    }

    public void testSimpleOffer() throws JSONException, InvalidDescriptionException {
        JSONObject simpleOffer = new JSONObject(sSimpleOffer);
        SessionDescription offer = SessionDescriptions.fromJsep(simpleOffer, mSdpProcessor);
        assertNotNull(offer);
        assertEquals(DescriptionType.INBOUND_OFFER, offer.getDescriptionType());
        assertEquals("1426854267315236600", offer.getSessionId());
        assertEquals(0, offer.getStreamDescriptionCount());
        assertFalse(offer.hasStreamType(StreamType.AUDIO));
        assertFalse(offer.hasStreamType(StreamType.VIDEO));
        assertFalse(offer.hasStreamType(StreamType.DATA));
    }

    public void testSimpleAnswer() throws JSONException, InvalidDescriptionException {
        JSONObject simpleAnswer = new JSONObject(sSimpleAnswer);
        SessionDescription answer = SessionDescriptions.fromJsep(simpleAnswer, mSdpProcessor);
        assertNotNull(answer);
        assertEquals(DescriptionType.INBOUND_ANSWER, answer.getDescriptionType());
        assertEquals("1426854267315236600", answer.getSessionId());
        assertEquals(0, answer.getStreamDescriptionCount());
        assertFalse(answer.hasStreamType(StreamType.AUDIO));
        assertFalse(answer.hasStreamType(StreamType.VIDEO));
        assertFalse(answer.hasStreamType(StreamType.DATA));
    }

    public void testChromeOffer() throws JSONException, InvalidDescriptionException {
        JSONObject json = new JSONObject(sChromeOffer);
        SessionDescription desc = SessionDescriptions.fromJsep(json, mSdpProcessor);
        assertNotNull(desc);
        assertEquals("7407423127539558064", desc.getSessionId());
        assertEquals(DescriptionType.INBOUND_OFFER, desc.getDescriptionType());
        assertEquals(2, desc.getStreamDescriptionCount());

        StreamDescription audio = desc.getStreamDescriptionByIndex(0);

        assertEquals(-1, audio.getMaxMessageSize());
        assertEquals(-1, audio.getSctpPort());
        assertNull(audio.getAppLabel());

        assertNotNull(audio.getCname());
        assertEquals("actpass", audio.getDtlsSetup());
        assertEquals("sha-256", audio.getFingerprintHashFunction());
        String fingerprint = "A8:B1:8B:70:89:B2:56:10:98:87:4F:A2:4A:0D:FE:76:C6:5C:8D:9D:4F:0B:12:4C:DB:B0:FC:08:8E:FF:B5:43";
        assertEquals(fingerprint, audio.getFingerprint());
        assertEquals("KJgfgjRFHmkuBHKhgoIHPkGpBRzErIAU4qQ3", audio.getId());
        assertEquals(4, audio.getCandidateCount());
        assertEquals("4000241536", audio.getCandidate(0).getFoundation());
        assertEquals(RtcCandidate.ComponentType.RTP, audio.getCandidate(0).getComponentType());
        assertEquals(RtcCandidate.TransportType.UDP, audio.getCandidate(0).getTransportType());
        assertEquals(2122260223, audio.getCandidate(0).getPriority());
        assertEquals("129.192.20.149", audio.getCandidate(0).getAddress());
        assertEquals(50238, audio.getCandidate(0).getPort());
        assertEquals(RtcCandidate.CandidateType.HOST, audio.getCandidate(0).getType());
        assertEquals("eNiJLDtGPTtx8J8b", audio.getCandidate(0).getUfrag());
        assertEquals("7f1lY4bUNTcw/DxFk4a0LY3j", audio.getCandidate(0).getPassword());
        assertEquals(StreamMode.SEND_RECEIVE, audio.getMode());
        assertEquals("eNiJLDtGPTtx8J8b", audio.getUfrag());
        assertEquals("7f1lY4bUNTcw/DxFk4a0LY3j", audio.getPassword());
        assertEquals(10, audio.getPayloadCount());
        assertEquals("opus", audio.getPayload(0).getEncodingName());
        assertEquals(111, audio.getPayload(0).getPayloadType());
        assertEquals(48000, audio.getPayload(0).getClockRate());
        assertEquals(2, audio.getPayload(0).getChannels());
        assertEquals(10, audio.getPayload(0).getParameters().get("minptime"));
        // the parameter isn't parsed properly
        assertNull(audio.getPayload(0).getParameters().get("useinbandfec"));
    }

    public void testInvalidType() throws JSONException {
        JSONObject json = new JSONObject(sInvalidType);
        assertNotNull(json);
        try {
            SessionDescriptions.fromJsep(json, mSdpProcessor);
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
            SessionDescriptions.fromJsep(json, mSdpProcessor);
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
            SessionDescriptions.fromJsep(json, mSdpProcessor);
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
            SessionDescriptions.fromJsep(json, mSdpProcessor);
        } catch (InvalidDescriptionException e) {
            assertTrue(e.getCause() instanceof JSONException);
            return;
        }
        throw new RuntimeException("should not be reached");
    }
}
