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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An uninstantiable class that provides utility methods for RtcCandidate
 */
public class RtcCandidates {
    private static final String TAG = "RtcCandidates";

    private RtcCandidates(){}

    /**
     * Converts a candidate to a JSEP candidate object
     * @param candidate the candidate to transform
     * @return a new JSON object following the JSEP format, or null if the candidate doesn't have any context information
     */
    public static JSONObject toJsep(RtcCandidate candidate) {
        if (candidate == null) {
            throw new NullPointerException("candidate should not be null");
        }
        JSONObject json = new JSONObject();
        if (candidate.getStreamIndex() < 0 && candidate.getStreamId() == null) {
            return null;
        }
        try {
            if (candidate.getStreamId() != null) {
                json.put("sdpMid", candidate.getStreamId());
            }
            if (candidate.getStreamIndex() >= 0) {
                json.put("sdpMLineIndex", candidate.getStreamIndex());
            }
            json.put("candidate", toSdpAttribute(candidate));
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.wtf(TAG, "failed to generate jsep candidate", e);
            return null;
        }
    }

    /**
     * Converts a JSEP candidate object to a RtcCandidate
     * @param json a JSEP candidate object
     * @return a new RtcCandidate
     */
    public static RtcCandidate fromJsep(JSONObject json) {
        if (json == null) {
            throw new NullPointerException("json should not be null");
        }
        String candidateLine = json.optString("candidate");
        if (candidateLine == null) {
            return null;
        }
        int sdpMLineIndex = json.optInt("sdpMLineIndex", -1);
        String sdpMid = json.isNull("sdpMid") ? null : json.optString("sdpMid", null);
        if (sdpMLineIndex < 0 && sdpMid == null) {
            return null;
        }
        RtcCandidateImpl rtcCandidate = (RtcCandidateImpl) fromSdpAttribute(candidateLine);
        if (rtcCandidate == null) {
            return null;
        }
        rtcCandidate.setStreamIndex(sdpMLineIndex);
        rtcCandidate.setStreamId(sdpMid);

        return rtcCandidate;
    }

    /**
     * Converts a candidate to a candidate attribute string with the format "candidate:..."
     * @param candidate the candidate to convert
     * @return a candidate attribute string
     */
    public static String toSdpAttribute(RtcCandidate candidate) {
        final StringBuilder sb = new StringBuilder("candidate:");
        sb.append(candidate.getFoundation());
        sb.append(" ");
        switch (candidate.getComponentType()) {
            case RTP:
                sb.append(1);
                break;
            case RTCP:
                sb.append(2);
                break;
        }
        sb.append(" ");
        if (candidate.getTransportType() == RtcCandidate.TransportType.UDP) {
            sb.append("UDP");
        } else {
            sb.append("TCP");
        }
        sb.append(" ");
        sb.append(candidate.getPriority());
        sb.append(" ");
        sb.append(candidate.getAddress());
        sb.append(" ");
        sb.append(candidate.getPort());
        sb.append(" typ ");
        switch (candidate.getType()) {
            case HOST:
                sb.append("host");
                break;
            case SERVER_REFLEXIVE:
                sb.append("srflx");
                break;
            case PEER_REFLEXIVE:
                sb.append("prflx");
                break;
            case RELAY:
                sb.append("relay");
                break;
        }
        if (candidate.getRelatedAddress() != null) {
            sb.append(" raddr ");
            sb.append(candidate.getRelatedAddress());
            sb.append(" rport ");
            sb.append(candidate.getRelatedPort());
        }
        if (candidate.getTransportType() != RtcCandidate.TransportType.UDP) {
            sb.append(" tcptype ");
            switch (candidate.getTransportType()) {
                case TCP_ACTIVE:
                    sb.append("active");
                    break;
                case TCP_PASSIVE:
                    sb.append("passive");
                    break;
                case TCP_SO:
                    sb.append("so");
                    break;
            }
        }
        return sb.toString();
    }

    private static Pattern sSdpCandidateAttributePattern = Pattern.compile("^" +
            "(?:a=)?candidate:" +
            "(\\d+) " + // foundation
            "(1|2) " + // component
            "(UDP|TCP) " + // transport
            "([\\d\\.]*) " + // priority
            "([\\d\\.a-f:]*) " + // address, ipv4 or ipv6
            "(\\d*)" + // port
            " typ (host|srflx|prflx|relay)" + // type
            "(?: raddr ([\\d\\.a-f:]*) rport (\\d*))?" + // reflexive address and port
            "(?: tcptype (active|passive|so))?" + // tcp type
            ".*" + // ignore the rest
            "(?:\\r\\n)?$"
            , Pattern.CASE_INSENSITIVE
    );

    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_FOUNDATION = 1;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_COMPONENT = 2;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_TRANSPORT = 3;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_PRIORITY = 4;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_ADDRESS = 5;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_PORT = 6;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_TYPE = 7;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_RELATED_ADDRESS = 8;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_RELATED_PORT = 9;
    @SuppressWarnings("FieldCanBeLocal") private static int PATTERN_GROUP_ATTRIBUTE_TCP_TYPE = 10;

    /**
     * Parses an attribute line of the format "candidate:...." with an optional "a=" prefix
     *
     * @param candidateAttribute the attribute string
     * @return a valid RtcCandidate without context information, or null if the attribute string could not be parsed
     */
    public static RtcCandidate fromSdpAttribute(String candidateAttribute) {
        Matcher matcher = sSdpCandidateAttributePattern.matcher(candidateAttribute);
        if (!matcher.matches()) {
            return null;
        }

        String foundation = matcher.group(PATTERN_GROUP_ATTRIBUTE_FOUNDATION);
        int componentId = Integer.parseInt(matcher.group(PATTERN_GROUP_ATTRIBUTE_COMPONENT), 10);
        RtcCandidate.ComponentType componentType;
        String transport = matcher.group(PATTERN_GROUP_ATTRIBUTE_TRANSPORT);
        int priority = Integer.parseInt(matcher.group(PATTERN_GROUP_ATTRIBUTE_PRIORITY), 10);
        String address = matcher.group(PATTERN_GROUP_ATTRIBUTE_ADDRESS);
        int port = Integer.parseInt(matcher.group(PATTERN_GROUP_ATTRIBUTE_PORT), 10);
        String type = matcher.group(PATTERN_GROUP_ATTRIBUTE_TYPE);
        RtcCandidate.CandidateType candidateType;
        String relatedAddress = matcher.group(PATTERN_GROUP_ATTRIBUTE_RELATED_ADDRESS);
        int relatedPort = -1;
        if (relatedAddress != null) {
            relatedPort = Integer.parseInt(matcher.group(PATTERN_GROUP_ATTRIBUTE_RELATED_PORT), 10);
        }
        String tcpType = matcher.group(PATTERN_GROUP_ATTRIBUTE_TCP_TYPE);
        RtcCandidate.TransportType transportType;

        if (componentId == 1) {
            componentType = RtcCandidate.ComponentType.RTP;
        } else {
            componentType = RtcCandidate.ComponentType.RTCP;
        }

        switch (type.toLowerCase()) {
            case "host":
                candidateType = RtcCandidate.CandidateType.HOST;
                break;
            case "srflx":
                candidateType = RtcCandidate.CandidateType.SERVER_REFLEXIVE;
                break;
            case "prflx":
                candidateType = RtcCandidate.CandidateType.PEER_REFLEXIVE;
                break;
            case "relay":
                candidateType = RtcCandidate.CandidateType.RELAY;
                break;
            default:
                return null;
        }

        if ("udp".equals(transport.toLowerCase())) {
            transportType = RtcCandidate.TransportType.UDP;
        } else {
            if (tcpType != null) {
                switch (tcpType.toLowerCase()) {
                    case "active":
                        transportType = RtcCandidate.TransportType.TCP_ACTIVE;
                        break;
                    case "passive":
                        transportType = RtcCandidate.TransportType.TCP_PASSIVE;
                        break;
                    case "so":
                        transportType = RtcCandidate.TransportType.TCP_SO;
                        break;
                    default:
                        return null;
                }
            } else if (port == 0 || port == 9) {
                transportType = RtcCandidate.TransportType.TCP_ACTIVE;
                port = 9;
            } else {
                return null;
            }
        }

        if (relatedAddress == null) {
            if (candidateType == RtcCandidate.CandidateType.HOST) {
                return new RtcCandidateImpl(foundation, componentType, transportType, priority, address, port, candidateType, null, -1);
            } else {
                return null;
            }
        } else {
            return new RtcCandidateImpl(foundation, componentType, transportType, priority, address, port, candidateType, relatedAddress, relatedPort);
        }
    }
}
