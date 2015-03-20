/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import android.util.Log;
import android.util.Pair;

import com.ericsson.research.owr.AudioPayload;
import com.ericsson.research.owr.Candidate;
import com.ericsson.research.owr.CandidateType;
import com.ericsson.research.owr.CodecType;
import com.ericsson.research.owr.ComponentType;
import com.ericsson.research.owr.Payload;
import com.ericsson.research.owr.TransportType;
import com.ericsson.research.owr.VideoPayload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SessionDescriptions {
    public static final String TAG = "SessionDescriptions";

    public static SessionDescription fromJsep(JSONObject json, SdpProcessor sdpProcessor) throws InvalidDescriptionException {
        String type;
        String sdpStr;
        try {
            type = json.getString("type");
        } catch (JSONException e) {
            throw new InvalidDescriptionException("jsep message has no type", e);
        }
        try {
            sdpStr = json.getString("sdp");
        } catch (JSONException e) {
            throw new InvalidDescriptionException("jsep message has no sdp", e);
        }
        JSONObject sdp = sdpProcessor.sdpToJson(sdpStr);

        DescriptionType descriptionType;
        if ("offer".equals(type)) {
            descriptionType = DescriptionType.INBOUND_OFFER;
        } else if ("answer".equals(type)) {
            descriptionType = DescriptionType.INBOUND_ANSWER;
        } else {
            throw new InvalidDescriptionException("invalid jsep message type: " + type);
        }

        JSONObject originator;
        try {
            originator = sdp.getJSONObject("originator");
        } catch (JSONException e) {
            throw new InvalidDescriptionException("sdp has no originator", e);
        }

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
        ArrayList<StreamDescription> streamDescriptions = new ArrayList<StreamDescription>(mediaDescriptions.length());

        for (int i = 0; i < mediaDescriptions.length(); i++) {
            JSONObject mediaDescription = mediaDescriptions.optJSONObject(i);
            if (mediaDescription == null) {
                return null;
            }
            try {
                streamDescriptions.add(mediaDescriptionJsonToStreamDescription(mediaDescription));
            } catch (JSONException e) {
                throw new InvalidDescriptionException("Failed to parse media description", e);
            }
        }

        return new SessionDescriptionImpl(descriptionType, sessionId, streamDescriptions);
    }

    private static StreamDescription mediaDescriptionJsonToStreamDescription(JSONObject json) throws JSONException, InvalidDescriptionException {
        StreamType streamType;
        StreamMode mode;
        String id;
        String ufrag;
        String password;
        List<Candidate> candidates = null;
        String dtlsSetup;
        String fingerprint;
        String fingerprintHashFunction;

        String cname = null;
        boolean rtcpMux;
        List<Long> ssrcs = null;
        List<Pair<Payload, Map<String, Object>>> payloads = null;

        int sctpPort;
        int maxMessageSize;
        String appLabel;

        String type = json.getString("type");
        if ("audio".equals(type)) {
            streamType = StreamType.AUDIO;
        } else if ("video".equals(type)) {
            streamType = StreamType.VIDEO;
        } else if ("application".equals(type)) {
            streamType = StreamType.DATA;
        } else {
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

        JSONObject ice = json.getJSONObject("ice");
        ufrag = ice.getString("ufrag");
        password = ice.getString("password");

        if (streamType == StreamType.DATA) {
            rtcpMux = false;
        } else {
            JSONObject rtcp = json.optJSONObject("rtcp");
            rtcpMux = rtcp != null && rtcp.optBoolean("mux", false);
        }

        JSONArray candidateArr = ice.optJSONArray("candidates");
        if (candidateArr != null) {
            candidates = new ArrayList<Candidate>(candidateArr.length());

            for (int i = 0; i < candidateArr.length(); i++) {
                try {
                    candidates.add(owrJsonToCandidate(candidateArr.getJSONObject(i), ufrag, password));
                } catch (JSONException exception) {
                    Log.w(TAG, "failed to read candidate: " + exception);
                }
            }
        }

        JSONObject dtls = json.getJSONObject("dtls");
        fingerprintHashFunction = dtls.getString("fingerprintHashFunction");
        fingerprint = dtls.getString("fingerprint");
        dtlsSetup = dtls.getString("setup");

        if (streamType == StreamType.DATA) {
            JSONObject sctp = json.getJSONObject("sctp");
            sctpPort = sctp.getInt("port");
            appLabel = sctp.getString("app");
            maxMessageSize = sctp.getInt("maxMessageSize");

            return new StreamDescriptionImpl(id, streamType, mode, ufrag, password, candidates, dtlsSetup, fingerprint, fingerprintHashFunction, sctpPort, maxMessageSize, appLabel);
        } else { // audio or video
            cname = json.optString("cname", null);

            JSONArray ssrcArr = json.optJSONArray("ssrcs");
            if (ssrcArr != null) {
                ssrcs = new ArrayList<Long>(ssrcArr.length());
                for (int i = 0; i < ssrcArr.length(); i++) {
                    ssrcs.add(ssrcArr.getLong(i));
                }
            }

            JSONArray payloadArr = json.getJSONArray("payloads");
            payloads = new ArrayList<Pair<Payload, Map<String, Object>>>(payloadArr.length());
            for (int i = 0; i < payloadArr.length(); i++) {
                JSONObject payload = payloadArr.getJSONObject(i);
                String encodingName = payload.getString("encodingName");
                try {
                    CodecType codecType = CodecType.valueOf(encodingName.toUpperCase());
                    int payloadType = payload.getInt("type");
                    Map<String, Object> parameters = jsonParametersToMap(payload.optJSONObject("parameters"));

                    int clockRate = payload.getInt("clockRate");

                    Payload owrPayload;
                    if (streamType == StreamType.VIDEO) {
                        boolean nackpli = payload.getBoolean("nackpli");
                        boolean ccmfir = payload.getBoolean("ccmfir");
                        owrPayload = new VideoPayload(codecType, payloadType, clockRate, nackpli, ccmfir);
                    } else { // audio
                        int channels = payload.getInt("channels");
                        owrPayload = new AudioPayload(codecType, payloadType, clockRate, channels);
                    }
                    payloads.add(new Pair<Payload, Map<String, Object>>(owrPayload, parameters));
                } catch (IllegalArgumentException ignored) {
                    Log.d(TAG, "ignoring payload with encoding name " + encodingName);
                }
            }
            return new StreamDescriptionImpl(id, streamType, mode, ufrag, password, candidates, dtlsSetup, fingerprint, fingerprintHashFunction, cname, rtcpMux, ssrcs, payloads);
        }
    }

    private static Map<String, Object> jsonParametersToMap(JSONObject json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        Map<String, Object> parameters = new HashMap<String, Object>();

        Iterator it = json.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            parameters.put(key, json.opt(key));
        }

        return parameters;
    }

    private static Candidate owrJsonToCandidate(JSONObject json, String ufrag, String password) throws JSONException {
        Candidate candidate = new Candidate(
                candidateTypeFromOwrJson(json),
                componentTypeFromOwrJson(json)
        );
        candidate.setTransportType(transportTypeFromOwrJson(json));
        candidate.setFoundation(json.getString("foundation"));
        candidate.setAddress(json.getString("address"));
        candidate.setPort(json.getInt("port"));
        candidate.setBaseAddress(json.optString("relatedAddress", ""));
        candidate.setBasePort(json.optInt("relatedPort", 0));
        candidate.setPriority(json.getInt("priority"));
        candidate.setUfrag(ufrag);
        candidate.setPassword(password);
        return candidate;
    }
    private static CandidateType candidateTypeFromOwrJson(JSONObject json) throws JSONException {
        String type = json.getString("type");
        if ("host".equals(type)) {
            return CandidateType.HOST;
        } else if ("srflx".equals(type)) {
            return CandidateType.SERVER_REFLEXIVE;
        } else if ("prflx".equals(type)) {
            return CandidateType.PEER_REFLEXIVE;
        } else if ("relay".equals(type)) {
            return CandidateType.RELAY;
        } else {
            throw new JSONException("unknown candidate type: " + type);
        }
    }

    private static ComponentType componentTypeFromOwrJson(JSONObject json) throws JSONException {
        int componentId = json.getInt("componentId");
        switch (componentId) {
            case 1:
                return ComponentType.RTP;
            case 2:
                return ComponentType.RTCP;
            default:
                throw new JSONException("unknown component id: " + componentId);
        }
    }

    private static TransportType transportTypeFromOwrJson(JSONObject json) throws JSONException {
        String transportType = json.getString("transport");
        if (transportType.equals("TCP")) {
            String tcpType = json.getString("tcpType");
            if ("active".equals(tcpType)) {
                return TransportType.TCP_ACTIVE;
            } else if ("passive".equals(tcpType)) {
                return TransportType.TCP_PASSIVE;
            } else if ("so".equals(tcpType)) {
                return TransportType.TCP_SO;
            } else {
                throw new JSONException("unknown tcp type: " + tcpType);
            }
        } else if (transportType.equals("UDP")) {
            return TransportType.UDP;
        } else {
            throw new JSONException("unknown transport type: " + transportType);
        }
    }
}
