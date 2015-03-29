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
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SdpProcessorTest extends AndroidTestCase {
    public static final String TAG = "SdpUtilsTest";

    private static final String sFfSdp = "" +
            "v=0\r\n" +
            "o=Mozilla-SIPUA-35.0.1 1021 0 IN IP4 0.0.0.0\r\n" +
            "s=SIP Call\r\n" +
            "t=0 0\r\n" +
            "a=ice-ufrag:90293b3d\r\n" +
            "a=ice-pwd:ab4334b9f3efb523ba45c11ec3152350\r\n" +
            "a=fingerprint:sha-256 6C:B4:AB:6C:86:7E:6C:C8:69:68:CE:53:A1:3E:36:D2:1D:5B:4E:CC:39:2E:C7:4D:3F:A8:04:B5:0E:EB:74:9D\r\n" +
            "m=audio 9 RTP/SAVPF 109 9 0 8 101\r\n" +
            "c=IN IP4 0.0.0.0\r\n" +
            "a=rtpmap:109 opus/48000/2\r\n" +
            "a=ptime:20\r\n" +
            "a=rtpmap:9 G722/8000\r\n" +
            "a=rtpmap:0 PCMU/8000\r\n" +
            "a=rtpmap:8 PCMA/8000\r\n" +
            "a=rtpmap:101 telephone-event/8000\r\n" +
            "a=fmtp:101 0-15\r\n" +
            "a=sendrecv\r\n" +
            "a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\r\n" +
            "a=setup:actpass\r\n" +
            "a=rtcp-mux\r\n" +
            "a=candidate:0 1 UDP 2130379007 172.20.10.2 51830 typ host\r\n" +
            "a=candidate:0 2 UDP 2130379006 172.20.10.2 59310 typ host\r\n" +
            "a=candidate:1 1 UDP 1694236671 90.237.24.157 41535 typ srflx raddr 172.20.10.2 rport 51830\r\n" +
            "a=candidate:0 1 UDP 2130379007 172.20.10.2 51830 typ host\r\n" +
            "a=candidate:0 2 UDP 2130379006 172.20.10.2 59310 typ host\r\n" +
            "a=candidate:1 1 UDP 1694236671 90.237.24.157 41535 typ srflx raddr 172.20.10.2 rport 51830\r\n" +
            "a=candidate:1 2 UDP 1694236670 90.237.24.157 48703 typ srflx raddr 172.20.10.2 rport 59310\r\n" +
            "a=candidate:3 1 UDP 100401151 192.36.158.14 64686 typ relay raddr 192.36.158.14 rport 64686\r\n" +
            "a=candidate:3 2 UDP 100401150 192.36.158.14 59208 typ relay raddr 192.36.158.14 rport 59208\r\n" +
            "m=video 9 RTP/SAVPF 120 126 97\r\n" +
            "c=IN IP4 0.0.0.0\r\n" +
            "a=rtpmap:120 VP8/90000\r\n" +
            "a=rtpmap:126 H264/90000\r\n" +
            "a=fmtp:126 PROFILE=0;LEVEL=0;profile-level-id=42e01f;packetization-mode=1;level-asymmetry-allowed=1;parameter-add=1;usedtx=0;stereo=0;useinbandfec=0;cbr=0\r\n" +
            "a=rtpmap:97 H264/90000\r\n" +
            "a=fmtp:97 PROFILE=0;LEVEL=0;profile-level-id=42e01f;packetization-mode=0;level-asymmetry-allowed=1;parameter-add=1;usedtx=0;stereo=0;useinbandfec=0;cbr=0\r\n" +
            "a=sendrecv\r\n" +
            "a=rtcp-fb:120 nack\r\n" +
            "a=rtcp-fb:120 nack pli\r\n" +
            "a=rtcp-fb:120 ccm fir\r\n" +
            "a=rtcp-fb:126 nack\r\n" +
            "a=rtcp-fb:126 nack pli\r\n" +
            "a=rtcp-fb:126 ccm fir\r\n" +
            "a=rtcp-fb:97 nack\r\n" +
            "a=rtcp-fb:97 nack pli\r\n" +
            "a=rtcp-fb:97 ccm fir\r\n" +
            "a=setup:actpass\r\n" +
            "a=rtcp-mux\r\n" +
            "a=candidate:0 1 UDP 2130379007 172.20.10.2 63183 typ host\r\n" +
            "a=candidate:0 2 UDP 2130379006 172.20.10.2 62121 typ host\r\n" +
            "a=candidate:0 1 UDP 2130379007 172.20.10.2 63183 typ host\r\n" +
            "a=candidate:0 2 UDP 2130379006 172.20.10.2 62121 typ host\r\n" +
            "a=candidate:1 1 UDP 1694236671 90.237.24.157 43849 typ srflx raddr 172.20.10.2 rport 63183\r\n" +
            "a=candidate:1 2 UDP 1694236670 90.237.24.157 39502 typ srflx raddr 172.20.10.2 rport 62121\r\n" +
            "a=candidate:3 1 UDP 100401151 192.36.158.14 56914 typ relay raddr 192.36.158.14 rport 56914\r\n" +
            "a=candidate:3 2 UDP 100401150 192.36.158.14 54995 typ relay raddr 192.36.158.14 rport 54995\r\n" +
            "m=application 9 DTLS/SCTP 5000\r\n" +
            "c=IN IP4 0.0.0.0\r\n" +
            "a=sctpmap:5000 webrtc-datachannel 256\r\n" +
            "a=setup:actpass\r\n" +
            "a=candidate:0 1 UDP 2130379007 172.20.10.2 51850 typ host\r\n" +
            "a=candidate:0 2 UDP 2130379006 172.20.10.2 61466 typ host\r\n" +
            "a=candidate:0 1 UDP 2130379007 172.20.10.2 51850 typ host\r\n" +
            "a=candidate:0 2 UDP 2130379006 172.20.10.2 61466 typ host\r\n" +
            "a=candidate:1 1 UDP 1694236671 90.237.24.157 35856 typ srflx raddr 172.20.10.2 rport 51850\r\n" +
            "a=candidate:1 2 UDP 1694236670 90.237.24.157 32790 typ srflx raddr 172.20.10.2 rport 61466\r\n" +
            "a=candidate:3 1 UDP 100401151 192.36.158.14 55300 typ relay raddr 192.36.158.14 rport 55300\r\n" +
            "a=candidate:3 2 UDP 100401150 192.36.158.14 61720 typ relay raddr 192.36.158.14 rport 61720\r\n";

    private static final String sDcSdp = "" +
            "v=0\r\n" +
            "o=- 128254989039880302 2 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "a=msid-semantic: WMS\r\n" +
            "m=application 57049 DTLS/SCTP 5000\r\n" +
            "c=IN IP4 192.168.1.86\r\n" +
            "a=candidate:3681649477 1 udp 2122260223 192.168.1.86 57049 typ host generation 0\r\n" +
            "a=ice-ufrag:9I+z5/4mb+Y00teq\r\n" +
            "a=ice-pwd:2gMDyNA8fu2WOCnIyyU1gkur\r\n" +
            "a=ice-options:google-ice\r\n" +
            "a=fingerprint:sha-256 1A:3C:A9:43:47:14:D1:12:E3:6E:C0:D5:19:14:EE:57:F6:FC:F9:1F:18:64:65:79:8B:AA:88:EB:3E:1A:B6:69\r\n" +
            "a=setup:actpass\r\n" +
            "a=mid:data\r\n" +
            "a=sctpmap:5000 webrtc-datachannel 1024\r\n";

    private static final String sSimpleSdp = "" +
            "v=0\r\n" +
            "o=- 1426854267315236600 1 IN IP4 127.0.0.1\r\n" +
            "s=-\r\n" +
            "t=0 0\r\n" +
            "m=application 0 NONE \r\n" +
            "c=IN IP4 0.0.0.0\r\n" +
            "a=sendrecv\r\n";
    private static final String sInvalidSdp = "y=application 0 NONE\r";

    public void testSimple() throws InvalidDescriptionException {
        JSONObject json = SdpProcessor.sdpToJson(sSimpleSdp);
        JSONArray mediaDescriptions = json.optJSONArray("mediaDescriptions");
        assertNotNull(mediaDescriptions);
        assertEquals(1, mediaDescriptions.length());
        assertEquals("application", mediaDescriptions.optJSONObject(0).optString("type"));
        assertEquals("0", mediaDescriptions.optJSONObject(0).optString("port"));
        assertEquals("NONE", mediaDescriptions.optJSONObject(0).optString("protocol"));
        String sdp = SdpProcessor.jsonToSdp(json);
        assertNotNull(sdp);
        assertEquals(sSimpleSdp, sdp);
    }

    public void testSimpleGen() throws InvalidDescriptionException, JSONException {
        String sdp = SdpProcessor.jsonToSdp(new JSONObject("{\"originator\":{\"sessionId\":123456789}}"));
        assertNotNull(sdp);
        assertEquals("v=0\r\no=- 123456789 1 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n", sdp);
    }

    public void testInvalidSdp() throws InvalidDescriptionException {
        JSONObject json = SdpProcessor.sdpToJson(sInvalidSdp);
        assertNotNull(json);
        JSONArray mediaDescriptions = json.optJSONArray("mediaDescriptions");
        assertNotNull(mediaDescriptions);
        assertEquals(0, mediaDescriptions.length());
    }

    public void testDatachannelSdp() throws InvalidDescriptionException {
        JSONObject json = SdpProcessor.sdpToJson(sDcSdp);
        assertNotNull(SdpProcessor.jsonToSdp(json));
        assertEquals("0", json.optString("version"));
        assertEquals("0", json.optString("startTime"));
        assertEquals("-", json.optString("sessionName"));
        assertEquals("0", json.optString("stopTime"));

        assertOriginator(json, 2, "128254989039880302", "-", "IN", "IP4", "127.0.0.1");

        JSONArray mediaDescriptions = json.optJSONArray("mediaDescriptions");
        assertNotNull(mediaDescriptions);
        assertEquals(1, mediaDescriptions.length());

        {
            JSONObject dataDescription = mediaDescriptions.optJSONObject(0);
            assertNotNull(dataDescription);
            assertEquals("application", dataDescription.optString("type"));
            assertEquals("57049", dataDescription.optString("port"));
            assertEquals("DTLS/SCTP", dataDescription.optString("protocol"));

            assertNetAttrs(dataDescription, "IN", "IP4", "192.168.1.86");
            assertSctp(dataDescription, 5000, "webrtc-datachannel", 1024);
            assertIce(dataDescription, "9I+z5/4mb+Y00teq", "2gMDyNA8fu2WOCnIyyU1gkur",
                    new IceCandidate("3681649477", 1, "UDP", 2122260223, "192.168.1.86", 57049, "host")
            );
            assertDtlsObject(dataDescription, "actpass", "sha-256", "1A:3C:A9:43:47:14:D1:12:E3:6E:C0:D5:19:14:EE:57:F6:FC:F9:1F:18:64:65:79:8B:AA:88:EB:3E:1A:B6:69");
        }
    }

    public void testFirefoxSdp() throws InvalidDescriptionException {
        JSONObject json = SdpProcessor.sdpToJson(sFfSdp);
        assertNotNull(SdpProcessor.jsonToSdp(json));
        assertEquals("0", json.optString("version"));
        assertEquals("0", json.optString("startTime"));
        assertEquals("SIP Call", json.optString("sessionName"));
        assertEquals("0", json.optString("stopTime"));

        assertOriginator(json, 0, "1021", "Mozilla-SIPUA-35.0.1", "IN", "IP4", "0.0.0.0");

        JSONArray mediaDescriptions = json.optJSONArray("mediaDescriptions");
        assertNotNull(mediaDescriptions);
        assertEquals(3, mediaDescriptions.length());

        {
            JSONObject audioDescription = mediaDescriptions.optJSONObject(0);
            assertNotNull(audioDescription);
            assertMediaAttrs(audioDescription, "audio", "9", "RTP/SAVPF");
            assertRtcpMux(audioDescription, true);
            assertEquals("sendrecv", audioDescription.optString("mode"));

            assertNetAttrs(audioDescription, "IN", "IP4", "0.0.0.0");
            assertDtlsObject(audioDescription, "actpass", "sha-256", "6C:B4:AB:6C:86:7E:6C:C8:69:68:CE:53:A1:3E:36:D2:1D:5B:4E:CC:39:2E:C7:4D:3F:A8:04:B5:0E:EB:74:9D");
            assertIce(audioDescription, "90293b3d", "ab4334b9f3efb523ba45c11ec3152350",
                    new IceCandidate("0", 2, "UDP", 2130379006, "172.20.10.2", 59310, "host"),
                    new IceCandidate("0", 1, "UDP", 2130379007, "172.20.10.2", 51830, "host"),
                    new IceCandidate("1", 1, "UDP", 1694236671, "90.237.24.157", 41535, "srflx", "172.20.10.2", 51830),
                    new IceCandidate("0", 1, "UDP", 2130379007, "172.20.10.2", 51830, "host"),
                    new IceCandidate("0", 2, "UDP", 2130379006, "172.20.10.2", 59310, "host"),
                    new IceCandidate("1", 1, "UDP", 1694236671, "90.237.24.157", 41535, "srflx", "172.20.10.2", 51830),
                    new IceCandidate("1", 2, "UDP", 1694236670, "90.237.24.157", 48703, "srflx", "172.20.10.2", 59310),
                    new IceCandidate("3", 1, "UDP", 100401151, "192.36.158.14", 64686, "relay", "192.36.158.14", 64686),
                    new IceCandidate("3", 2, "UDP", 100401150, "192.36.158.14", 59208, "relay", "192.36.158.14", 59208)
            );
            assertPayloads(audioDescription,
                    new MediaPayload(0, 8000, "PCMU", 1, null),
                    new MediaPayload(8, 8000, "PCMA", 1, null),
                    new MediaPayload(9, 8000, "G722", 1, null),
                    new MediaPayload(101, 8000, "telephone-event", 1, null),
                    new MediaPayload(109, 48000, "opus", 2, null)
            );
        }

        {
            JSONObject videoDescription = mediaDescriptions.optJSONObject(1);
            assertNotNull(videoDescription);
            assertMediaAttrs(videoDescription, "video", "9", "RTP/SAVPF");
            assertRtcpMux(videoDescription, true);
            assertEquals("sendrecv", videoDescription.optString("mode"));

            assertNetAttrs(videoDescription, "IN", "IP4", "0.0.0.0");
            assertDtlsObject(videoDescription, "actpass", "sha-256", "6C:B4:AB:6C:86:7E:6C:C8:69:68:CE:53:A1:3E:36:D2:1D:5B:4E:CC:39:2E:C7:4D:3F:A8:04:B5:0E:EB:74:9D");
            assertIce(videoDescription, "90293b3d", "ab4334b9f3efb523ba45c11ec3152350",
                    new IceCandidate("0", 1, "UDP", 2130379007, "172.20.10.2", 63183, "host"),
                    new IceCandidate("0", 2, "UDP", 2130379006, "172.20.10.2", 62121, "host"),
                    new IceCandidate("0", 1, "UDP", 2130379007, "172.20.10.2", 63183, "host"),
                    new IceCandidate("0", 2, "UDP", 2130379006, "172.20.10.2", 62121, "host"),
                    new IceCandidate("1", 1, "UDP", 1694236671, "90.237.24.157", 43849, "srflx", "172.20.10.2", 63183),
                    new IceCandidate("1", 2, "UDP", 1694236670, "90.237.24.157", 39502, "srflx", "172.20.10.2", 62121),
                    new IceCandidate("3", 1, "UDP", 100401151, "192.36.158.14", 56914, "relay", "192.36.158.14", 56914),
                    new IceCandidate("3", 2, "UDP", 100401150, "192.36.158.14", 54995, "relay", "192.36.158.14", 54995)
            );
            assertPayloads(videoDescription,
                    new MediaPayload(97, 90000, "H264", true, true, true, new HashMap<String, Object>(){{
                        put("packetizationMode", 0);
                        put("levelAsymmetryAllowed", 1);
                        put("PROFILE", 0);
                        put("cbr", 0);
                        put("useinbandfec", 0);
                        put("profileLevelId", "42e01f");
                        put("stereo", 0);
                        put("LEVEL", 0);
                        put("usedtx", 0);
                        put("parameterAdd", 1);
                    }}),
                    new MediaPayload(126, 90000, "H264", true, true, true, new HashMap<String, Object>(){{
                        put("packetizationMode", 1);
                        put("levelAsymmetryAllowed", 1);
                        put("PROFILE", 0);
                        put("cbr", 0);
                        put("useinbandfec", 0);
                        put("profileLevelId", "42e01f");
                        put("stereo", 0);
                        put("LEVEL", 0);
                        put("usedtx", 0);
                        put("parameterAdd", 1);
                    }}),
                    new MediaPayload(120, 90000, "VP8", true, true, true, null)
            );
        }

        {
            JSONObject dataDescription = mediaDescriptions.optJSONObject(2);
            assertNotNull(dataDescription);
            assertMediaAttrs(dataDescription, "application", "9", "DTLS/SCTP");

            assertSctp(dataDescription, 5000, "webrtc-datachannel", 256);
            assertNetAttrs(dataDescription, "IN", "IP4", "0.0.0.0");
            assertDtlsObject(dataDescription, "actpass", "sha-256", "6C:B4:AB:6C:86:7E:6C:C8:69:68:CE:53:A1:3E:36:D2:1D:5B:4E:CC:39:2E:C7:4D:3F:A8:04:B5:0E:EB:74:9D");
            assertIce(dataDescription, "90293b3d", "ab4334b9f3efb523ba45c11ec3152350",
                    new IceCandidate("0", 1, "UDP", 2130379007, "172.20.10.2", 51850, "host"),
                    new IceCandidate("0", 2, "UDP", 2130379006, "172.20.10.2", 61466, "host"),
                    new IceCandidate("0", 1, "UDP", 2130379007, "172.20.10.2", 51850, "host"),
                    new IceCandidate("0", 2, "UDP", 2130379006, "172.20.10.2", 61466, "host"),
                    new IceCandidate("1", 1, "UDP", 1694236671, "90.237.24.157", 35856, "srflx", "172.20.10.2", 51850),
                    new IceCandidate("1", 2, "UDP", 1694236670, "90.237.24.157", 32790, "srflx", "172.20.10.2", 61466),
                    new IceCandidate("3", 1, "UDP", 100401151, "192.36.158.14", 55300, "relay", "192.36.158.14", 55300),
                    new IceCandidate("3", 2, "UDP", 100401150, "192.36.158.14", 61720, "relay", "192.36.158.14", 61720)
            );
        }
    }

    private void assertRtcpMux(JSONObject mediaDescription, boolean rtcpMux) {
        JSONObject rtcp = mediaDescription.optJSONObject("rtcp");
        if (rtcpMux) {
            assertNotNull(rtcp);
            assertTrue(rtcp.optBoolean("mux", false));
        } else {
            assertTrue(rtcp == null || !rtcp.optBoolean("mux", false));
        }
    }

    private void assertMediaAttrs(JSONObject mediaDescription, String type, String port, String protocol) {
        assertEquals(type, mediaDescription.optString("type"));
        assertEquals(port, mediaDescription.optString("port"));
        assertEquals(protocol, mediaDescription.optString("protocol"));
    }

    private void assertOriginator(JSONObject json, int sessionVersion, String sessionId, String username, String netType, String addressType, String address) {
        JSONObject originator = json.optJSONObject("originator");
        assertNotNull(originator);
        assertEquals(sessionVersion, originator.optInt("sessionVersion"));
        assertEquals(sessionId, originator.optString("sessionId", null));
        assertEquals(username, originator.optString("username"));
        assertNetAttrs(originator, netType, addressType, address);
    }

    private void assertPayloads(JSONObject mediaDescription, MediaPayload... payloads) {
        JSONArray jsonPayloads = mediaDescription.optJSONArray("payloads");

        if (payloads.length == 0) {
            assertTrue(jsonPayloads == null || jsonPayloads.length() == 0);
            return;
        }
        assertNotNull(jsonPayloads);
        assertEquals(payloads.length, jsonPayloads.length());

        SparseArray<MediaPayload> payloadMap = new SparseArray<>();
        for (MediaPayload payload : payloads) {
            payloadMap.put(payload.mPayloadType, payload);
        }

        for (int i = 0; i < jsonPayloads.length(); i++) {
            JSONObject jsonPayload = jsonPayloads.optJSONObject(i);
            assertNotNull(jsonPayload);
            int payloadType = jsonPayload.optInt("type");
            MediaPayload payload = payloadMap.get(payloadType);
            assertNotNull(payload);
            payload.assertMatch(jsonPayload);
        }
    }

    private void assertIce(JSONObject mediaDescription, String ufrag, String password, IceCandidate... candidates) {
        JSONObject ice = mediaDescription.optJSONObject("ice");
        assertNotNull(ice);
        assertEquals(ufrag, ice.optString("ufrag"));
        assertEquals(password, ice.optString("password"));

        JSONArray jsonCandidates = ice.optJSONArray("candidates");

        if (candidates.length == 0) {
            assertTrue(jsonCandidates == null || jsonCandidates.length() == 0);
            return;
        }
        assertNotNull(jsonCandidates);
        assertEquals(candidates.length, jsonCandidates.length());
        LinkedList<IceCandidate> remainingCandidates = new LinkedList<>(Arrays.asList(candidates));

        for (int i = 0; i < jsonCandidates.length(); i++) {
            JSONObject jsonCandidate = jsonCandidates.optJSONObject(i);
            assertNotNull(jsonCandidate);

            IceCandidate chosenCandidate = null;
            for (IceCandidate candidate : remainingCandidates) {
                if (candidate.isMatch(jsonCandidate)) {
                    chosenCandidate = candidate;
                    break;
                }
            }
            assertNotNull(chosenCandidate);
            remainingCandidates.remove(chosenCandidate);
        }
    }

    private void assertNetAttrs(JSONObject json, String netType, String addressType, String address) {
        assertEquals(address, json.optString("address"));
        assertEquals(netType, json.optString("netType"));
        assertEquals(addressType, json.optString("addressType"));
    }

    private void assertSctp(JSONObject mediaDescription, int port, String app, int streamCount) {
        JSONObject sctp = mediaDescription.optJSONObject("sctp");
        assertNotNull(sctp);
        assertEquals(port, sctp.optInt("port"));
        assertEquals(app, sctp.optString("app"));
        assertEquals(streamCount, sctp.optInt("streams"));
    }

    private void assertDtlsObject(JSONObject mediaDescription, String setup, String hashFunc, String fingerprint) {
        JSONObject dtls = mediaDescription.optJSONObject("dtls");
        assertNotNull(dtls);
        assertNotNull(dtls);
        assertEquals(setup, dtls.optString("setup"));
        assertEquals(hashFunc, dtls.optString("fingerprintHashFunction"));
        assertEquals(fingerprint, dtls.optString("fingerprint"));
    }

    private class IceCandidate {
        private final String mFoundation;
        private final int mComponentId;
        private final String mTransport;
        private final long mPriority;
        private final String mAddress;
        private final int mPort;
        private final String mRelatedAddress;
        private final String mType;
        private final int mRelatedPort;
        private final String mTcpType;

        public IceCandidate(String foundation, int componentId, String transport, long priority, String address, int port, String type, String relatedAddress, int relatedPort, String tcpType) {
            mFoundation = foundation;
            mComponentId = componentId;
            mTransport = transport;
            mPriority = priority;
            mAddress = address;
            mPort = port;
            mType = type;
            mRelatedAddress = relatedAddress;
            mRelatedPort = relatedPort;
            mTcpType = tcpType;
        }

        public IceCandidate(String foundation, int componentId, String transport, long priority, String address, int port, String type, String relatedAddress, int relatedPort) {
            this(foundation, componentId, transport, priority, address, port, type, relatedAddress, relatedPort, null);
        }

        public IceCandidate(String foundation, int componentId, String transport, long priority, String address, int port, String type) {
            this(foundation, componentId, transport, priority, address, port, type, null, -1, null);
        }

        public boolean isMatch(JSONObject json) {
            if (!mFoundation.equals(json.optString("foundation"))) {
                return false;
            }
            if (mComponentId != json.optInt("componentId")) {
                return false;
            }
            if (!mTransport.equals(json.optString("transport"))) {
                return false;
            }
            if (mPriority != json.optLong("priority")) {
                return false;
            }
            if (!mAddress.equals(json.optString("address"))) {
                return false;
            }
            if (mPort != json.optInt("port")) {
                return false;
            }
            if (!mType.equals(json.optString("type"))) {
                return false;
            }
            if (mRelatedAddress != null) {
                if (!mRelatedAddress.equals(json.optString("relatedAddress"))) {
                    return false;
                }
            }
            if (mRelatedPort > 0) {
                if (mRelatedPort != json.optInt("relatedPort")) {
                    return false;
                }
            }
            if (mTcpType != null) {
                if (!mTcpType.equals(json.optString("tcpType"))) {
                    return false;
                }
            }
            return true;
        }
    }

    private class MediaPayload {
        private final int mPayloadType;
        private final int mChannels;
        private final int mClockRate;
        private final String mEncodingName;
        private final Boolean mNackPli;
        private final Boolean mNack;
        private final Boolean mCcmFir;
        private final HashMap<String, Object> mParameters;

        private MediaPayload(int payloadType, int clockRate, String encodingName, int channels, Boolean nackPli, Boolean nack, Boolean ccmFir, HashMap<String, Object> parameters) {
            mPayloadType = payloadType;
            mChannels = channels;
            mClockRate = clockRate;
            mEncodingName = encodingName;
            mNackPli = nackPli;
            mNack = nack;
            mCcmFir = ccmFir;
            mParameters = parameters;
        }

        public MediaPayload(int payloadType, int clockRate, String encodingName, Boolean nackPli, Boolean nack, Boolean ccmFir, HashMap<String, Object> parameters) {
            this(payloadType, clockRate, encodingName, -1, nackPli, nack, ccmFir, parameters);
        }

        public MediaPayload(int payloadType, int clockRate, String encodingName, int channels, HashMap<String, Object> parameters) {
            this(payloadType, clockRate, encodingName, channels, null, null, null, parameters);
        }

        public void assertMatch(JSONObject json) {
            assertEquals(mPayloadType, json.optInt("type"));
            assertEquals(mClockRate, json.optInt("clockRate"));
            assertEquals(mEncodingName, json.optString("encodingName"));
            if (mChannels > 0) {
                assertEquals(mChannels, json.optInt("channels"));
            }
            if (mNackPli != null) {
                json.has("nackpli");
                assertEquals((boolean) mNackPli, json.optBoolean("nackpli"));
            }
            if (mNack != null) {
                json.has("nack");
                assertEquals((boolean) mNack, json.optBoolean("nack"));
            }
            if (mCcmFir != null) {
                json.has("ccmfir");
                assertEquals((boolean) mCcmFir, json.optBoolean("ccmfir"));
            }
            JSONObject jsonParameters = json.optJSONObject("parameters");
            if (mParameters != null) {
                assertNotNull(jsonParameters);
                assertEquals(mParameters.size(), jsonParameters.length());

                for (Map.Entry<String, Object> parameter : mParameters.entrySet()) {
                    assertTrue(jsonParameters.has(parameter.getKey()));
                    assertEquals(parameter.getValue(), jsonParameters.opt(parameter.getKey()));
                }
            } else {
                assertTrue(jsonParameters == null || jsonParameters.length() == 0);
            }
        }
    }
}
