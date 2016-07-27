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

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SessionDescriptions {
    private static final String TAG = "SessionDescriptions";

    private SessionDescriptions() {}

    public static SessionDescription fromJsep(JSONObject json) throws InvalidDescriptionException {
        String type;
        JSONObject sdp;

        String sdpStr;
        try {
            type = json.getString("type");
        } catch (JSONException e) {
            throw new InvalidDescriptionException("jsep message has no type", e);
        }

        sdp = json.optJSONObject("sessionDescription");
        if (sdp == null) {
            try {
                sdpStr = json.getString("sdp");
            } catch (JSONException e) {
                throw new InvalidDescriptionException("jsep message has no sdp", e);
            }
            sdp = SdpProcessor.sdpToJson(sdpStr);
        }

        SessionDescription.Type descriptionType;
        switch (type) {
            case "offer":
                descriptionType = SessionDescription.Type.OFFER;
                break;
            case "answer":
                descriptionType = SessionDescription.Type.ANSWER;
                break;
            default:
                throw new InvalidDescriptionException("invalid jsep message type: " + type);
        }

        JSONObject originator;
        originator = sdp.optJSONObject("originator");

        String sessionId = null;
        if (originator != null) {
            sessionId = originator.optString("sessionId", null);
        }

        JSONArray mediaDescriptions;
        try {
            mediaDescriptions = sdp.getJSONArray("mediaDescriptions");
        } catch (JSONException e) {
            throw new InvalidDescriptionException("no media descriptions found");
        }

        if (mediaDescriptions.length() == 0) {
            Log.w(TAG, "media descriptions array is empty");
        }
        ArrayList<StreamDescription> streamDescriptions = new ArrayList<>(mediaDescriptions.length());

        for (int i = 0; i < mediaDescriptions.length(); i++) {
            JSONObject mediaDescription = mediaDescriptions.optJSONObject(i);
            if (mediaDescription == null) {
                return null;
            }
            try {
                streamDescriptions.add(mediaDescriptionJsonToStreamDescription(mediaDescription, i));
            } catch (JSONException e) {
                throw new InvalidDescriptionException("Failed to parse media description", e);
            }
        }

        return new SessionDescriptionImpl(descriptionType, sessionId, streamDescriptions);
    }

    private static StreamDescription mediaDescriptionJsonToStreamDescription(JSONObject json, int index) throws JSONException, InvalidDescriptionException {
        StreamType streamType;
        StreamMode mode;
        String id;
        String ufrag = null;
        String password = null;
        List<RtcCandidate> candidates = null;
        String dtlsSetup;
        String fingerprint;
        String fingerprintHashFunction;

        String mediaStreamId;
        String mediaStreamTrackId;
        String cname;
        boolean rtcpMux;
        List<Long> ssrcs = null;
        List<RtcPayload> payloads;

        int sctpPort;
        int sctpMaxMessageSize;
        int sctpStreamCount;
        String appLabel;

        String type = json.getString("type");
        switch (type) {
            case "audio":
                streamType = StreamType.AUDIO;
                break;
            case "video":
                streamType = StreamType.VIDEO;
                break;
            case "application":
                streamType = StreamType.DATA;
                break;
            default:
                throw new InvalidDescriptionException("invalid type: " + type);
        }

        String modeString = json.optString("mode", null);
        if (modeString == null) {
            mode = StreamMode.SEND_RECEIVE;
        } else if (modeString.equals("sendrecv")) {
            mode = StreamMode.SEND_RECEIVE;
        } else if (modeString.equals("recvonly")) {
            mode = StreamMode.RECEIVE_ONLY;
        } else if (modeString.equals("sendonly")) {
            mode = StreamMode.SEND_ONLY;
        } else if (modeString.equals("inactive")) {
            mode = StreamMode.INACTIVE;
        } else {
            throw new InvalidDescriptionException(type + " has invalid mode: " + modeString);
        }

        id = json.optString("mediaStreamId", null);

        if (streamType == StreamType.DATA) {
            rtcpMux = false;
        } else {
            JSONObject rtcp = json.optJSONObject("rtcp");
            rtcpMux = rtcp != null && rtcp.optBoolean("mux", false);
        }

        JSONObject ice = json.optJSONObject("ice");
        if (ice != null) {
            ufrag = ice.getString("ufrag");
            password = ice.getString("password");

            JSONArray candidateArr = ice.optJSONArray("candidates");
            if (candidateArr != null) {
                candidates = new ArrayList<>(candidateArr.length());

                for (int i = 0; i < candidateArr.length(); i++) {
                    try {
                        candidates.add(owrJsonToCandidate(candidateArr.getJSONObject(i), ufrag, password, index, id));
                    } catch (JSONException exception) {
                        Log.w(TAG, "failed to read candidate: " + exception);
                    }
                }
            }
        }

        JSONObject dtls = json.getJSONObject("dtls");
        fingerprintHashFunction = dtls.getString("fingerprintHashFunction");
        fingerprint = dtls.getString("fingerprint");
        dtlsSetup = dtls.optString("setup");

        if (streamType == StreamType.DATA) {
            JSONObject sctp = json.getJSONObject("sctp");
            sctpPort = sctp.getInt("port");
            appLabel = sctp.getString("app");
            sctpStreamCount = sctp.optInt("streams", -1);

            return new StreamDescriptionImpl(streamType, mode, ufrag, password, candidates, dtlsSetup, fingerprint, fingerprintHashFunction, sctpPort, sctpStreamCount, appLabel);
        } else { // audio or video
            cname = json.optString("cname", null);
            mediaStreamId = json.optString("mediaStreamId", null);
            mediaStreamTrackId = json.optString("mediaStreamTrackId", null);

            JSONArray ssrcArr = json.optJSONArray("ssrcs");
            if (ssrcArr != null) {
                ssrcs = new ArrayList<>(ssrcArr.length());
                for (int i = 0; i < ssrcArr.length(); i++) {
                    ssrcs.add(ssrcArr.getLong(i));
                }
            }

            JSONArray payloadArr = json.getJSONArray("payloads");
            payloads = new ArrayList<>(payloadArr.length());
            for (int i = 0; i < payloadArr.length(); i++) {
                JSONObject payload = payloadArr.getJSONObject(i);
                String encodingName = "<unknown>";
                try {
                    encodingName = payload.getString("encodingName");
                    int payloadType = payload.getInt("type");
                    Map<String, Object> parameters = jsonParametersToMap(payload.optJSONObject("parameters"));

                    int clockRate = payload.getInt("clockRate");
                    int channels = payload.optInt("channels", 0);
                    boolean nack = payload.optBoolean("nack", false);
                    boolean nackPli = payload.optBoolean("nackpli", false);
                    boolean ccmFir = payload.optBoolean("ccmfir", false);

                    payloads.add(new RtcPayloadImpl(payloadType, encodingName, clockRate, parameters, channels, nack, nackPli, ccmFir));
                } catch (JSONException e) {
                    Log.d(TAG, "ignoring payload \"" + encodingName + "\": " + e.getMessage());
                }
            }
            return new StreamDescriptionImpl(streamType, mode, ufrag, password, candidates, dtlsSetup, fingerprint, fingerprintHashFunction, mediaStreamId, mediaStreamTrackId, cname, rtcpMux, ssrcs, payloads);
        }
    }

    private static Map<String, Object> jsonParametersToMap(JSONObject json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        Map<String, Object> parameters = new HashMap<>();

        Iterator it = json.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            parameters.put(key, json.opt(key));
        }

        return parameters;
    }

    private static RtcCandidate owrJsonToCandidate(JSONObject json, String ufrag, String password, int index, String id) throws JSONException {
        String foundation = json.getString("foundation");
        RtcCandidate.ComponentType componentType = componentTypeFromOwrJson(json);
        String address = json.getString("address");
        int port = json.getInt("port");
        int priority = json.getInt("priority");
        RtcCandidate.TransportType transportType = transportTypeFromOwrJson(json);
        RtcCandidate.CandidateType type = candidateTypeFromOwrJson(json);
        String relatedAddress = json.optString("relatedAddress", "");
        int relatedPort = json.optInt("relatedPort", 0);
        return new RtcCandidateImpl(index, id, ufrag, password, foundation, componentType, transportType, priority, address, port, type, relatedAddress, relatedPort);
    }
    private static RtcCandidate.CandidateType candidateTypeFromOwrJson(JSONObject json) throws JSONException {
        String type = json.getString("type");
        switch (type) {
            case "host":
                return RtcCandidate.CandidateType.HOST;
            case "srflx":
                return RtcCandidate.CandidateType.SERVER_REFLEXIVE;
            case "prflx":
                return RtcCandidate.CandidateType.PEER_REFLEXIVE;
            case "relay":
                return RtcCandidate.CandidateType.RELAY;
            default:
                throw new JSONException("unknown candidate type: " + type);
        }
    }

    private static RtcCandidate.ComponentType componentTypeFromOwrJson(JSONObject json) throws JSONException {
        int componentId = json.getInt("componentId");
        switch (componentId) {
            case 1:
                return RtcCandidate.ComponentType.RTP;
            case 2:
                return RtcCandidate.ComponentType.RTCP;
            default:
                throw new JSONException("unknown component id: " + componentId);
        }
    }

    private static RtcCandidate.TransportType transportTypeFromOwrJson(JSONObject json) throws JSONException {
        String transportType = json.getString("transport");
        switch (transportType) {
            case "TCP":
                String tcpType = json.getString("tcpType");
                switch (tcpType) {
                    case "active":
                        return RtcCandidate.TransportType.TCP_ACTIVE;
                    case "passive":
                        return RtcCandidate.TransportType.TCP_PASSIVE;
                    case "so":
                        return RtcCandidate.TransportType.TCP_SO;
                    default:
                        throw new JSONException("unknown tcp type: " + tcpType);
                }
            case "UDP":
                return RtcCandidate.TransportType.UDP;
            default:
                throw new JSONException("unknown transport type: " + transportType);
        }
    }

    public static JSONObject toJsep(SessionDescription sessionDescription) {
        JSONObject json = new JSONObject();
        String type;
        String sdpStr;
        JSONObject description;

        switch (sessionDescription.getType()) {
            case ANSWER:
                type = "answer";
                break;
            case OFFER:
                type = "offer";
                break;
            default:
                throw new IllegalArgumentException("invalid description type: " + sessionDescription.getType());
        }

        try {
            JSONObject sdpJson = sessionDescriptionToOwrJson(sessionDescription);
            sdpStr = SdpProcessor.jsonToSdp(sdpJson);
        } catch (InvalidDescriptionException | JSONException e) {
            throw new IllegalArgumentException("json to sdp conversion failed: " + e.getMessage(), e);
        }

        try {
            description = sessionDescriptionToOwrJson(sessionDescription);
        } catch (JSONException e) {
            throw new IllegalArgumentException("json to sessionDescription and SDP conversion failed: " + e.getMessage(), e);
        }

        try {
            json.put("type", type);
            json.put("sdp", sdpStr);
            json.put("sessionDescription", description);
            return json;
        } catch (JSONException e) {
            throw new IllegalArgumentException("failed to create jsep message: " + e.getMessage(), e);
        }
    }

    private static JSONObject sessionDescriptionToOwrJson(SessionDescription sessionDescription) throws JSONException {
        JSONObject json = new JSONObject();

        JSONObject originator = new JSONObject();
        originator.put("username", "-");
        originator.put("sessionId", sessionDescription.getSessionId());
        originator.put("sessionVersion", 1);
        originator.put("netType", "IN");
        originator.put("addressType", "IP4");
        originator.put("address", "127.0.0.1");

        json.put("version", 0);
        json.put("originator", originator);
        json.put("sessionName", "-");
        json.put("startTime", 0);
        json.put("stopTime", 0);

        JSONArray streamDescriptions = new JSONArray();
        for (StreamDescription streamDescription : sessionDescription.getStreamDescriptions()) {
            streamDescriptions.put(streamDescriptionToOwrJson(streamDescription));
        }

        json.put("mediaDescriptions", streamDescriptions);

        return json;
    }

    private static JSONObject streamDescriptionToOwrJson(StreamDescription streamDescription) throws JSONException {
        JSONObject json = new JSONObject();
        String type = "unknown";

        switch (streamDescription.getType()) {
            case AUDIO:
                type = "audio";
                break;
            case VIDEO:
                type = "video";
                break;
            case DATA:
                type = "application";
                break;
        }
        json.put("type", type);

        if (streamDescription.getType() == StreamType.DATA) {
            json.put("protocol", "DTLS/SCTP");
        } else {
            json.put("protocol", "RTP/SAVPF");
        }

        switch (streamDescription.getMode()) {
            case SEND_RECEIVE:
                json.put("mode", "sendrecv");
                break;
            case SEND_ONLY:
                json.put("mode", "sendonly");
                break;
            case RECEIVE_ONLY:
                json.put("mode", "recvonly");
                break;
            case INACTIVE:
                json.put("mode", "inactive");
                break;
        }
        json.put("cname", streamDescription.getCname());
        json.put("mediaStreamId", streamDescription.getMediaStreamId());
        json.put("mediaStreamTrackId", streamDescription.getMediaStreamTrackId());

        if (streamDescription.getType() != StreamType.DATA) {
            JSONArray payloads = new JSONArray();
            for (RtcPayload payload : streamDescription.getPayloads()) {
                JSONObject jsonPayload = new JSONObject();

                jsonPayload.put("type", payload.getPayloadType());
                jsonPayload.put("encodingName", payload.getEncodingName());
                jsonPayload.put("clockRate", payload.getClockRate());

                if (payload.getParameters() != null) {
                    JSONObject parameters = new JSONObject();
                    for (Map.Entry<String, Object> parameter : payload.getParameters().entrySet()) {
                        parameters.put(parameter.getKey(), parameter.getValue());
                    }
                    jsonPayload.put("parameters", parameters);
                }

                if (streamDescription.getType() == StreamType.VIDEO) {
                    jsonPayload.put("nack", payload.isNack());
                    jsonPayload.put("nackpli", payload.isNackPli());
                    jsonPayload.put("ccmfir", payload.isCcmFir());
                } else {
                    jsonPayload.put("channels", payload.getChannels());
                }
                payloads.put(jsonPayload);
            }
            json.put("payloads", payloads);
        } else {
            JSONObject sctp = new JSONObject();
            sctp.put("port", streamDescription.getSctpPort());
            sctp.put("app", streamDescription.getAppLabel());
            if (streamDescription.getSctpStreamCount() >= 0) {
                sctp.put("streams", streamDescription.getSctpStreamCount());
            }
            json.put("sctp", sctp);
        }

        if (streamDescription.isRtcpMux()) {
            JSONObject rtcp = new JSONObject();
            rtcp.put("mux", true);
            json.put("rtcp", rtcp);
        }

        if (!streamDescription.getSsrcs().isEmpty()) {
            JSONArray ssrcs = new JSONArray();
            for (long ssrc : streamDescription.getSsrcs()) {
                ssrcs.put(ssrc);
            }
            json.put("ssrcs", ssrcs);
        }

        JSONObject ice = new JSONObject();
        ice.put("ufrag", streamDescription.getUfrag());
        ice.put("password", streamDescription.getPassword());

        JSONArray candidates = new JSONArray();
        for (RtcCandidate candidate : streamDescription.getCandidates()) {
            candidates.put(candidateToJson(candidate));
        }
        ice.put("candidates", candidates);

        json.put("ice", ice);

        JSONObject dtls = new JSONObject();
        dtls.put("fingerprintHashFunction", streamDescription.getFingerprintHashFunction());
        dtls.put("fingerprint", streamDescription.getFingerprint());
        dtls.put("setup", streamDescription.getDtlsSetup());
        json.put("dtls", dtls);

        return json;
    }

    private static JSONObject candidateToJson(RtcCandidate candidate) throws JSONException {
        JSONObject json = new JSONObject();

        switch (candidate.getType()) {
            case HOST:
                json.put("type", "host");
                break;
            case SERVER_REFLEXIVE:
                json.put("type", "srflx");
                break;
            case PEER_REFLEXIVE:
                json.put("type", "prflx");
                break;
            case RELAY:
                json.put("type", "relay");
                break;
            default:
                throw new IllegalArgumentException("invalid candidate type: " + candidate.getType());
        }

        switch (candidate.getTransportType()) {
            case UDP:
                json.put("transport", "UDP");
                break;
            case TCP_ACTIVE:
                json.put("tcpType", "active");
                json.put("transport", "TCP");
                break;
            case TCP_PASSIVE:
                json.put("tcpType", "passive");
                json.put("transport", "TCP");
                break;
            case TCP_SO:
                json.put("tcpType", "so");
                json.put("transport", "TCP");
                break;
            default:
                throw new IllegalArgumentException("invalid transport type: " + candidate.getType());
        }

        json.put("foundation", candidate.getFoundation());
        json.put("componentId", candidate.getComponentType().ordinal() + 1);
        json.put("priority", candidate.getPriority());
        json.put("address", candidate.getAddress());

        int port = candidate.getPort();
        json.put("port", port != 0 ? port : 9);

        if (candidate.getType() != RtcCandidate.CandidateType.HOST) {
            json.put("relatedAddress", candidate.getRelatedAddress());
            int basePort = candidate.getRelatedPort();
            json.put("relatedPort", basePort != 0 ? basePort : 9);
        }

        return json;
    }
}
