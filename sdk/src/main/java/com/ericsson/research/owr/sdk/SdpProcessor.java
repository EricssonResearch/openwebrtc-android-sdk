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

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

class SdpProcessor {
    private static final String TAG = "SdpProcessor";

    private static SdpProcessor sInstance = null;

    private final ScriptableObject mScope;
    private final Function mSdpToJsonFunction;
    private final Function mJsonToSdpFunction;

    public ScriptableObject getScope() {
        return mScope;
    }

    public Function getSdpToJsonFunction() {
        return mSdpToJsonFunction;
    }

    public Function getJsonToSdpFunction() {
        return mJsonToSdpFunction;
    }

    private SdpProcessor() {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_8);
        mScope = context.initStandardObjects();

        try {
            context.evaluateString(mScope, sSdpJsSource, "sdp.js", 1, null);
            mSdpToJsonFunction = context.compileFunction(mScope, "function sdpToJson(sdp) { return JSON.stringify(SDP.parse(sdp)); }", "sdpToJson", 1, null);
            mJsonToSdpFunction = context.compileFunction(mScope, "function jsonToSdp(sdp) { return SDP.generate(JSON.parse(sdp)); }", "jsonToSdp", 1, null);
        } finally {
            Context.exit();
        }
    }

    static JSONObject sdpToJson(String sdp) throws InvalidDescriptionException {
        synchronized (SdpProcessor.class) {
            if (sInstance == null) {
                sInstance = new SdpProcessor();
            }
        }
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            ScriptableObject scope = sInstance.getScope();
            Object result = sInstance.getSdpToJsonFunction().call(context, scope, scope, new Object[]{sdp});
            try {
                return new JSONObject(result.toString());
            } catch (JSONException e) {
                throw new InvalidDescriptionException("failed to parse json generated from SDP", e);
            }
        } catch (EvaluatorException e) {
            throw new InvalidDescriptionException("failed to parse sdp: " + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    static String jsonToSdp(JSONObject json) throws InvalidDescriptionException {
        synchronized (SdpProcessor.class) {
            if (sInstance == null) {
                sInstance = new SdpProcessor();
            }
        }
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            ScriptableObject scope = sInstance.getScope();
            Object result = sInstance.getJsonToSdpFunction().call(context, scope, scope, new Object[]{json.toString()});
            return "" + result;
        } catch (EvaluatorException e) {
            throw new InvalidDescriptionException("failed to parse sdp: " + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    // TODO: generate this from source at build time
    private static final String sSdpJsSource = "" +"\"use strict\";\n" +
            "\n" +
            "if (typeof(SDP) == \"undefined\")\n" +
            "    var SDP = {};\n" +
            "\n" +
            "(function () {\n" +
            "    var regexps = {\n" +
            "        \"vline\": \"^v=([\\\\d]+).*$\",\n" +
            "        \"oline\": \"^o=([\\\\w\\\\-@\\\\.]+) ([\\\\d]+) ([\\\\d]+) IN (IP[46]) ([\\\\d\\\\.a-f\\\\:]+).*$\",\n" +
            "        \"sline\": \"^s=(.*)$\",\n" +
            "        \"tline\": \"^t=([\\\\d]+) ([\\\\d]+).*$\",\n" +
            "        \"cline\": \"^c=IN (IP[46]) ([\\\\d\\\\.a-f\\\\:]+).*$\",\n" +
            "        \"msidsemantic\": \"^a=msid-semantic: *WMS .*$\",\n" +
            "        \"mblock\": \"^m=(audio|video|application) ([\\\\d]+) ([A-Z/]+)([\\\\d ]*)$\\\\r?\\\\n\",\n" +
            "        \"mode\": \"^a=(sendrecv|sendonly|recvonly|inactive).*$\",\n" +
            "        \"rtpmap\": \"^a=rtpmap:${type} ([\\\\w\\\\-]+)/([\\\\d]+)/?([\\\\d]+)?.*$\",\n" +
            "        \"fmtp\": \"^a=fmtp:${type} ([\\\\w\\\\-=;]+).*$\",\n" +
            "        \"param\": \"([\\\\w\\\\-]+)=([\\\\w\\\\-]+);?\",\n" +
            "        \"nack\": \"^a=rtcp-fb:${type} nack$\",\n" +
            "        \"nackpli\": \"^a=rtcp-fb:${type} nack pli$\",\n" +
            "        \"ccmfir\": \"^a=rtcp-fb:${type} ccm fir$\",\n" +
            "        \"rtcp\": \"^a=rtcp:([\\\\d]+)( IN (IP[46]) ([\\\\d\\\\.a-f\\\\:]+))?.*$\",\n" +
            "        \"rtcpmux\": \"^a=rtcp-mux.*$\",\n" +
            "        \"cname\": \"^a=ssrc:(\\\\d+) cname:([\\\\w+/\\\\-@\\\\.]+).*$\",\n" +
            "        \"msid\": \"^a=(ssrc:\\\\d+ )?msid:([\\\\w+/\\\\-=]+) +([\\\\w+/\\\\-=]+).*$\",\n" +
            "        \"ufrag\": \"^a=ice-ufrag:([\\\\w+/]*).*$\",\n" +
            "        \"pwd\": \"^a=ice-pwd:([\\\\w+/]*).*$\",\n" +
            "        \"candidate\": \"^a=candidate:(\\\\d+) (\\\\d) (UDP|TCP) ([\\\\d\\\\.]*) ([\\\\d\\\\.a-f\\\\:]*) (\\\\d*)\" +\n" +
            "            \" typ ([a-z]*)( raddr ([\\\\d\\\\.a-f\\\\:]*) rport (\\\\d*))?\" +\n" +
            "            \"( tcptype (active|passive|so))?.*$\",\n" +
            "        \"fingerprint\": \"^a=fingerprint:(sha-1|sha-256) ([A-Fa-f\\\\d\\:]+).*$\",\n" +
            "        \"setup\": \"^a=setup:(actpass|active|passive).*$\",\n" +
            "        \"sctpmap\": \"^a=sctpmap:${port} ([\\\\w\\\\-]+)( [\\\\d]+)?.*$\"\n" +
            "    };\n" +
            "\n" +
            "    var templates = {\n" +
            "        \"sdp\":\n" +
            "            \"v=${version}\\r\\n\" +\n" +
            "            \"o=${username} ${sessionId} ${sessionVersion} ${netType} ${addressType} ${address}\\r\\n\" +\n" +
            "            \"s=${sessionName}\\r\\n\" +\n" +
            "            \"t=${startTime} ${stopTime}\\r\\n\" +\n" +
            "            \"${msidsemanticLine}\",\n" +
            "\n" +
            "        \"msidsemantic\": \"a=msid-semantic:WMS ${mediaStreamIds}\\r\\n\",\n" +
            "\n" +
            "        \"mblock\":\n" +
            "            \"m=${type} ${port} ${protocol} ${fmt}\\r\\n\" +\n" +
            "            \"c=${netType} ${addressType} ${address}\\r\\n\" +\n" +
            "            \"${rtcpLine}\" +\n" +
            "            \"${rtcpMuxLine}\" +\n" +
            "            \"a=${mode}\\r\\n\" +\n" +
            "            \"${rtpMapLines}\" +\n" +
            "            \"${fmtpLines}\" +\n" +
            "            \"${nackLines}\" +\n" +
            "            \"${nackpliLines}\" +\n" +
            "            \"${ccmfirLines}\" +\n" +
            "            \"${cnameLines}\" +\n" +
            "            \"${msidLines}\" +\n" +
            "            \"${iceCredentialLines}\" +\n" +
            "            \"${candidateLines}\" +\n" +
            "            \"${dtlsFingerprintLine}\" +\n" +
            "            \"${dtlsSetupLine}\" +\n" +
            "            \"${sctpmapLine}\",\n" +
            "\n" +
            "        \"rtcp\": \"a=rtcp:${port}${[ ]netType}${[ ]addressType}${[ ]address}\\r\\n\",\n" +
            "        \"rtcpMux\": \"a=rtcp-mux\\r\\n\",\n" +
            "\n" +
            "        \"rtpMap\": \"a=rtpmap:${type} ${encodingName}/${clockRate}${[/]channels}\\r\\n\",\n" +
            "        \"fmtp\": \"a=fmtp:${type} ${parameters}\\r\\n\",\n" +
            "        \"nack\": \"a=rtcp-fb:${type} nack\\r\\n\",\n" +
            "        \"nackpli\": \"a=rtcp-fb:${type} nack pli\\r\\n\",\n" +
            "        \"ccmfir\": \"a=rtcp-fb:${type} ccm fir\\r\\n\",\n" +
            "\n" +
            "        \"cname\": \"a=ssrc:${ssrc} cname:${cname}\\r\\n\",\n" +
            "        \"msid\": \"a=${[ssrc:]ssrc[ ]}msid:${mediaStreamId} ${mediaStreamTrackId}\\r\\n\",\n" +
            "\n" +
            "        \"iceCredentials\":\n" +
            "            \"a=ice-ufrag:${ufrag}\\r\\n\" +\n" +
            "            \"a=ice-pwd:${password}\\r\\n\",\n" +
            "\n" +
            "        \"candidate\":\n" +
            "            \"a=candidate:${foundation} ${componentId} ${transport} ${priority} ${address} ${port}\" +\n" +
            "            \" typ ${type}${[ raddr ]relatedAddress}${[ rport ]relatedPort}${[ tcptype ]tcpType}\\r\\n\",\n" +
            "\n" +
            "        \"dtlsFingerprint\": \"a=fingerprint:${fingerprintHashFunction} ${fingerprint}\\r\\n\",\n" +
            "        \"dtlsSetup\": \"a=setup:${setup}\\r\\n\",\n" +
            "\n" +
            "        \"sctpmap\": \"a=sctpmap:${port} ${app}${[ ]streams}\\r\\n\"\n" +
            "    };\n" +
            "\n" +
            "    function match(data, pattern, flags, alt) {\n" +
            "        var r = new RegExp(pattern, flags);\n" +
            "        return data.match(r) || alt && alt.match(r) || null;\n" +
            "    }\n" +
            "\n" +
            "    function addDefaults(obj, defaults) {\n" +
            "        for (var p in defaults) {\n" +
            "            if (!defaults.hasOwnProperty(p))\n" +
            "                continue;\n" +
            "            if (typeof(obj[p]) == \"undefined\")\n" +
            "                obj[p] = defaults[p];\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    function fillTemplate(template, info) {\n" +
            "        var text = template;\n" +
            "        for (var p in info) {\n" +
            "            if (!info.hasOwnProperty(p))\n" +
            "                continue;\n" +
            "            var r = new RegExp(\"\\\\${(\\\\[[^\\\\]]+\\\\])?\" + p + \"(\\\\[[^\\\\]]+\\\\])?}\");\n" +
            "            text = text.replace(r, function (_, prefix, suffix) {\n" +
            "                if (!info[p] && info[p] != 0)\n" +
            "                    return \"\";\n" +
            "                prefix = prefix ? prefix.substr(1, prefix.length - 2) : \"\";\n" +
            "                suffix = suffix ? suffix.substr(1, suffix.length - 2) : \"\";\n" +
            "                return prefix + info[p] + suffix;\n" +
            "            });\n" +
            "        }\n" +
            "        return text;\n" +
            "    }\n" +
            "\n" +
            "    SDP.parse = function (sdpText) {\n" +
            "        sdpText = new String(sdpText);\n" +
            "        var sdpObj = {};\n" +
            "        var parts = sdpText.split(new RegExp(regexps.mblock, \"m\")) || [sdpText];\n" +
            "        var sblock = parts.shift();\n" +
            "        var version = parseInt((match(sblock, regexps.vline, \"m\") || [])[1]);\n" +
            "        if (!isNaN(version))\n" +
            "            sdpObj.version = version;\n" +
            "        var originator = match(sblock, regexps.oline, \"m\");;\n" +
            "        if (originator) {\n" +
            "            sdpObj.originator = {\n" +
            "                \"username\": originator[1],\n" +
            "                \"sessionId\": originator[2],\n" +
            "                \"sessionVersion\": parseInt(originator[3]),\n" +
            "                \"netType\": \"IN\",\n" +
            "                \"addressType\": originator[4],\n" +
            "                \"address\": originator[5]\n" +
            "            };\n" +
            "        }\n" +
            "        var sessionName = match(sblock, regexps.sline, \"m\");\n" +
            "        if (sessionName)\n" +
            "            sdpObj.sessionName = sessionName[1];\n" +
            "        var sessionTime = match(sblock, regexps.tline, \"m\");\n" +
            "        if (sessionTime) {\n" +
            "            sdpObj.startTime = parseInt(sessionTime[1]);\n" +
            "            sdpObj.stopTime = parseInt(sessionTime[2]);\n" +
            "        }\n" +
            "        var hasMediaStreamId = !!match(sblock, regexps.msidsemantic, \"m\");\n" +
            "        sdpObj.mediaDescriptions = [];\n" +
            "\n" +
            "        for (var i = 0; i < parts.length; i += 5) {\n" +
            "            var mediaDescription = {\n" +
            "                \"type\": parts[i],\n" +
            "                \"port\": parts[i + 1],\n" +
            "                \"protocol\": parts[i + 2],\n" +
            "            };\n" +
            "            var fmt = parts[i + 3].replace(/^[\\s\\uFEFF\\xA0]+/, '')\n" +
            "                .split(/ +/)\n" +
            "                .map(function (x) {\n" +
            "                    return parseInt(x);\n" +
            "                });\n" +
            "            var mblock = parts[i + 4];\n" +
            "\n" +
            "            var connection = match(mblock, regexps.cline, \"m\", sblock);\n" +
            "            if (connection) {\n" +
            "                mediaDescription.netType = \"IN\";\n" +
            "                mediaDescription.addressType = connection[1];\n" +
            "                mediaDescription.address = connection[2];\n" +
            "            }\n" +
            "            var mode = match(mblock, regexps.mode, \"m\", sblock);\n" +
            "            if (mode)\n" +
            "                mediaDescription.mode = mode[1];\n" +
            "\n" +
            "            var payloadTypes = [];\n" +
            "            if (match(mediaDescription.protocol, \"RTP/S?AVPF?\")) {\n" +
            "                mediaDescription.payloads = [];\n" +
            "                payloadTypes = fmt;\n" +
            "            }\n" +
            "            payloadTypes.forEach(function (payloadType) {\n" +
            "                var payload = { \"type\": payloadType };\n" +
            "                var rtpmapLine = fillTemplate(regexps.rtpmap, payload);\n" +
            "                var rtpmap = match(mblock, rtpmapLine, \"m\");\n" +
            "                if (rtpmap) {\n" +
            "                    payload.encodingName = rtpmap[1];\n" +
            "                    payload.clockRate = parseInt(rtpmap[2]);\n" +
            "                    if (mediaDescription.type == \"audio\")\n" +
            "                        payload.channels = parseInt(rtpmap[3]) || 1;\n" +
            "                    else if (mediaDescription.type == \"video\") {\n" +
            "                        var nackLine = fillTemplate(regexps.nack, payload);\n" +
            "                        payload.nack = !!match(mblock, nackLine, \"m\");\n" +
            "                        var nackpliLine = fillTemplate(regexps.nackpli, payload);\n" +
            "                        payload.nackpli = !!match(mblock, nackpliLine, \"m\");\n" +
            "                        var ccmfirLine = fillTemplate(regexps.ccmfir, payload);\n" +
            "                        payload.ccmfir = !!match(mblock, ccmfirLine, \"m\");\n" +
            "                    }\n" +
            "                } else if (payloadType == 0 || payloadType == 8) {\n" +
            "                    payload.encodingName = payloadType == 8 ? \"PCMA\" : \"PCMU\";\n" +
            "                    payload.clockRate = 8000;\n" +
            "                    payload.channels = 1;\n" +
            "                }\n" +
            "                var fmtpLine = fillTemplate(regexps.fmtp, payload);\n" +
            "                var fmtp = match(mblock, fmtpLine, \"m\");\n" +
            "                if (fmtp) {\n" +
            "                    payload.parameters = {};\n" +
            "                    fmtp[1].replace(new RegExp(regexps.param, \"g\"),\n" +
            "                        function(_, key, value) {\n" +
            "                            key = key.replace(/-([a-z])/g, function (_, c) {\n" +
            "                                return c.toUpperCase();\n" +
            "                            });\n" +
            "                            payload.parameters[key] = isNaN(+value) ? value : +value;\n" +
            "                    });\n" +
            "                }\n" +
            "                mediaDescription.payloads.push(payload);\n" +
            "            });\n" +
            "\n" +
            "            var rtcp = match(mblock, regexps.rtcp, \"m\");\n" +
            "            if (rtcp) {\n" +
            "                mediaDescription.rtcp = {\n" +
            "                    \"netType\": \"IN\",\n" +
            "                    \"port\": parseInt(rtcp[1])\n" +
            "                };\n" +
            "                if (rtcp[2]) {\n" +
            "                    mediaDescription.rtcp.addressType = rtcp[3];\n" +
            "                    mediaDescription.rtcp.address = rtcp[4];\n" +
            "                }\n" +
            "            }\n" +
            "            var rtcpmux = match(mblock, regexps.rtcpmux, \"m\", sblock);\n" +
            "            if (rtcpmux) {\n" +
            "                if (!mediaDescription.rtcp)\n" +
            "                    mediaDescription.rtcp = {};\n" +
            "                mediaDescription.rtcp.mux = true;\n" +
            "            }\n" +
            "\n" +
            "            var cnameLines = match(mblock, regexps.cname, \"mg\");\n" +
            "            if (cnameLines) {\n" +
            "                mediaDescription.ssrcs = [];\n" +
            "                cnameLines.forEach(function (line) {\n" +
            "                    var cname = match(line, regexps.cname, \"m\");\n" +
            "                    mediaDescription.ssrcs.push(parseInt(cname[1]));\n" +
            "                    if (!mediaDescription.cname)\n" +
            "                        mediaDescription.cname = cname[2];\n" +
            "                });\n" +
            "            }\n" +
            "\n" +
            "            if (hasMediaStreamId) {\n" +
            "                var msid = match(mblock, regexps.msid, \"m\");\n" +
            "                if (msid) {\n" +
            "                    mediaDescription.mediaStreamId = msid[2];\n" +
            "                    mediaDescription.mediaStreamTrackId = msid[3];\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            var ufrag = match(mblock, regexps.ufrag, \"m\", sblock);\n" +
            "            var pwd = match(mblock, regexps.pwd, \"m\", sblock);\n" +
            "            if (ufrag && pwd) {\n" +
            "                mediaDescription.ice = {\n" +
            "                    \"ufrag\": ufrag[1],\n" +
            "                    \"password\": pwd[1]\n" +
            "                };\n" +
            "            }\n" +
            "            var candidateLines = match(mblock, regexps.candidate, \"mig\");\n" +
            "            if (candidateLines) {\n" +
            "                if (!mediaDescription.ice)\n" +
            "                    mediaDescription.ice = {};\n" +
            "                mediaDescription.ice.candidates = [];\n" +
            "                candidateLines.forEach(function (line) {\n" +
            "                    var candidateLine = match(line, regexps.candidate, \"mi\");\n" +
            "                    var candidate = {\n" +
            "                        \"foundation\": candidateLine[1],\n" +
            "                        \"componentId\": parseInt(candidateLine[2]),\n" +
            "                        \"transport\": candidateLine[3].toUpperCase(),\n" +
            "                        \"priority\": parseInt(candidateLine[4]),\n" +
            "                        \"address\": candidateLine[5],\n" +
            "                        \"port\": parseInt(candidateLine[6]),\n" +
            "                        \"type\": candidateLine[7]\n" +
            "                    };\n" +
            "                    if (candidateLine[9])\n" +
            "                        candidate.relatedAddress = candidateLine[9];\n" +
            "                    if (!isNaN(candidateLine[10]))\n" +
            "                        candidate.relatedPort = parseInt(candidateLine[10]);\n" +
            "                    if (candidateLine[12])\n" +
            "                        candidate.tcpType = candidateLine[12];\n" +
            "                    else if (candidate.transport == \"TCP\") {\n" +
            "                        if (candidate.port == 0 || candidate.port == 9) {\n" +
            "                            candidate.tcpType = \"active\";\n" +
            "                            candidate.port = 9;\n" +
            "                        } else {\n" +
            "                            return;\n" +
            "                        }\n" +
            "                    }\n" +
            "                    mediaDescription.ice.candidates.push(candidate);\n" +
            "                });\n" +
            "            }\n" +
            "\n" +
            "            var fingerprint = match(mblock, regexps.fingerprint, \"mi\", sblock);\n" +
            "            if (fingerprint) {\n" +
            "                mediaDescription.dtls = {\n" +
            "                    \"fingerprintHashFunction\": fingerprint[1].toLowerCase(),\n" +
            "                    \"fingerprint\": fingerprint[2].toUpperCase()\n" +
            "                };\n" +
            "            }\n" +
            "            var setup = match(mblock, regexps.setup, \"m\", sblock);\n" +
            "            if (setup) {\n" +
            "                if (!mediaDescription.dtls)\n" +
            "                    mediaDescription.dtls = {};\n" +
            "                mediaDescription.dtls.setup = setup[1];\n" +
            "            }\n" +
            "\n" +
            "            if (mediaDescription.protocol == \"DTLS/SCTP\") {\n" +
            "                mediaDescription.sctp = {\n" +
            "                    \"port\": fmt[0]\n" +
            "                };\n" +
            "                var sctpmapLine = fillTemplate(regexps.sctpmap, mediaDescription.sctp);\n" +
            "                var sctpmap = match(mblock, sctpmapLine, \"m\");\n" +
            "                if (sctpmap) {\n" +
            "                    mediaDescription.sctp.app = sctpmap[1];\n" +
            "                    if (sctpmap[2])\n" +
            "                        mediaDescription.sctp.streams = parseInt(sctpmap[2]);\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            sdpObj.mediaDescriptions.push(mediaDescription);\n" +
            "        }\n" +
            "\n" +
            "        return sdpObj;\n" +
            "    };\n" +
            "\n" +
            "    SDP.generate = function (sdpObj) {\n" +
            "        sdpObj = JSON.parse(JSON.stringify(sdpObj));\n" +
            "        addDefaults(sdpObj, {\n" +
            "            \"version\": 0,\n" +
            "            \"originator\": {},\n" +
            "            \"sessionName\": \"-\",\n" +
            "            \"startTime\": 0,\n" +
            "            \"stopTime\": 0,\n" +
            "            \"mediaDescriptions\": []\n" +
            "        });\n" +
            "        addDefaults(sdpObj.originator, {\n" +
            "            \"username\": \"-\",\n" +
            "            \"sessionId\": \"\" + Math.floor((Math.random() + +new Date()) * 1e6),\n" +
            "            \"sessionVersion\": 1,\n" +
            "            \"netType\": \"IN\",\n" +
            "            \"addressType\": \"IP4\",\n" +
            "            \"address\": \"127.0.0.1\"\n" +
            "        });\n" +
            "        var sdpText = fillTemplate(templates.sdp, sdpObj);\n" +
            "        sdpText = fillTemplate(sdpText, sdpObj.originator);\n" +
            "\n" +
            "        var msidsemanticLine = \"\";\n" +
            "        var mediaStreamIds = [];\n" +
            "        sdpObj.mediaDescriptions.forEach(function (mdesc) {\n" +
            "            if (mdesc.mediaStreamId && mdesc.mediaStreamTrackId\n" +
            "                && mediaStreamIds.indexOf(mdesc.mediaStreamId) == -1)\n" +
            "                mediaStreamIds.push(mdesc.mediaStreamId);\n" +
            "        });\n" +
            "        if (mediaStreamIds.length) {\n" +
            "            var msidsemanticLine = fillTemplate(templates.msidsemantic,\n" +
            "                { \"mediaStreamIds\": mediaStreamIds.join(\" \") });\n" +
            "        }\n" +
            "        sdpText = fillTemplate(sdpText, { \"msidsemanticLine\": msidsemanticLine });\n" +
            "\n" +
            "        sdpObj.mediaDescriptions.forEach(function (mediaDescription) {\n" +
            "            addDefaults(mediaDescription, {\n" +
            "                \"port\": 1,\n" +
            "                \"protocol\": \"RTP/SAVPF\",\n" +
            "                \"netType\": \"IN\",\n" +
            "                \"addressType\": \"IP4\",\n" +
            "                \"address\": \"0.0.0.0\",\n" +
            "                \"mode\": \"sendrecv\",\n" +
            "                \"payloads\": [],\n" +
            "                \"rtcp\": {}\n" +
            "            });\n" +
            "            var mblock = fillTemplate(templates.mblock, mediaDescription);\n" +
            "\n" +
            "            var payloadInfo = {\"rtpMapLines\": \"\", \"fmtpLines\": \"\", \"nackLines\": \"\",\n" +
            "                \"nackpliLines\": \"\", \"ccmfirLines\": \"\"};\n" +
            "            mediaDescription.payloads.forEach(function (payload) {\n" +
            "                if (payloadInfo.fmt)\n" +
            "                    payloadInfo.fmt += \" \" + payload.type;\n" +
            "                else\n" +
            "                    payloadInfo.fmt = payload.type;\n" +
            "                if (!payload.channels || payload.channels == 1)\n" +
            "                    payload.channels = null;\n" +
            "                payloadInfo.rtpMapLines += fillTemplate(templates.rtpMap, payload);\n" +
            "                if (payload.parameters) {\n" +
            "                    var fmtpInfo = { \"type\": payload.type, \"parameters\": \"\" };\n" +
            "                    for (var p in payload.parameters) {\n" +
            "                        var param = p.replace(/([A-Z])([a-z])/g, function (_, a, b) {\n" +
            "                            return \"-\" + a.toLowerCase() + b;\n" +
            "                        });\n" +
            "                        if (fmtpInfo.parameters)\n" +
            "                            fmtpInfo.parameters += \";\";\n" +
            "                        fmtpInfo.parameters += param + \"=\" + payload.parameters[p];\n" +
            "                    }\n" +
            "                    payloadInfo.fmtpLines += fillTemplate(templates.fmtp, fmtpInfo);\n" +
            "                }\n" +
            "                if (payload.nack)\n" +
            "                    payloadInfo.nackLines += fillTemplate(templates.nack, payload);\n" +
            "                if (payload.nackpli)\n" +
            "                    payloadInfo.nackpliLines += fillTemplate(templates.nackpli, payload);\n" +
            "                if (payload.ccmfir)\n" +
            "                    payloadInfo.ccmfirLines += fillTemplate(templates.ccmfir, payload);\n" +
            "            });\n" +
            "            mblock = fillTemplate(mblock, payloadInfo);\n" +
            "\n" +
            "            var rtcpInfo = {\"rtcpLine\": \"\", \"rtcpMuxLine\": \"\"};\n" +
            "            if (mediaDescription.rtcp.port) {\n" +
            "                addDefaults(mediaDescription.rtcp, {\n" +
            "                    \"netType\": \"IN\",\n" +
            "                    \"addressType\": \"IP4\",\n" +
            "                    \"address\": \"\"\n" +
            "                });\n" +
            "                if (!mediaDescription.rtcp.address)\n" +
            "                    mediaDescription.rtcp.netType = mediaDescription.rtcp.addressType = \"\";\n" +
            "                rtcpInfo.rtcpLine = fillTemplate(templates.rtcp, mediaDescription.rtcp);\n" +
            "            }\n" +
            "            if (mediaDescription.rtcp.mux)\n" +
            "                rtcpInfo.rtcpMuxLine = templates.rtcpMux;\n" +
            "            mblock = fillTemplate(mblock, rtcpInfo);\n" +
            "\n" +
            "            var srcAttributeLines = { \"cnameLines\": \"\", \"msidLines\": \"\" };\n" +
            "            var srcAttributes = {\n" +
            "                \"cname\": mediaDescription.cname,\n" +
            "                \"mediaStreamId\": mediaDescription.mediaStreamId,\n" +
            "                \"mediaStreamTrackId\": mediaDescription.mediaStreamTrackId\n" +
            "            };\n" +
            "            if (mediaDescription.cname && mediaDescription.ssrcs) {\n" +
            "                mediaDescription.ssrcs.forEach(function (ssrc) {\n" +
            "                    srcAttributes.ssrc = ssrc;\n" +
            "                    srcAttributeLines.cnameLines += fillTemplate(templates.cname, srcAttributes);\n" +
            "                    if (mediaDescription.mediaStreamId && mediaDescription.mediaStreamTrackId)\n" +
            "                        srcAttributeLines.msidLines += fillTemplate(templates.msid, srcAttributes);\n" +
            "                });\n" +
            "            } else if (mediaDescription.mediaStreamId && mediaDescription.mediaStreamTrackId) {\n" +
            "                srcAttributes.ssrc = null;\n" +
            "                srcAttributeLines.msidLines += fillTemplate(templates.msid, srcAttributes);\n" +
            "            }\n" +
            "            mblock = fillTemplate(mblock, srcAttributeLines);\n" +
            "\n" +
            "            var iceInfo = {\"iceCredentialLines\": \"\", \"candidateLines\": \"\"};\n" +
            "            if (mediaDescription.ice) {\n" +
            "                iceInfo.iceCredentialLines = fillTemplate(templates.iceCredentials,\n" +
            "                    mediaDescription.ice);\n" +
            "                if (mediaDescription.ice.candidates) {\n" +
            "                    mediaDescription.ice.candidates.forEach(function (candidate) {\n" +
            "                        addDefaults(candidate, {\n" +
            "                            \"relatedAddress\": null,\n" +
            "                            \"relatedPort\": null,\n" +
            "                            \"tcpType\": null\n" +
            "                        });\n" +
            "                        iceInfo.candidateLines += fillTemplate(templates.candidate, candidate);\n" +
            "                    });\n" +
            "                }\n" +
            "            }\n" +
            "            mblock = fillTemplate(mblock, iceInfo);\n" +
            "\n" +
            "            var dtlsInfo = { \"dtlsFingerprintLine\": \"\", \"dtlsSetupLine\": \"\" };\n" +
            "            if (mediaDescription.dtls) {\n" +
            "                if (mediaDescription.dtls.fingerprint) {\n" +
            "                    dtlsInfo.dtlsFingerprintLine = fillTemplate(templates.dtlsFingerprint,\n" +
            "                        mediaDescription.dtls);\n" +
            "                }\n" +
            "                addDefaults(mediaDescription.dtls, {\"setup\": \"actpass\"});\n" +
            "                dtlsInfo.dtlsSetupLine = fillTemplate(templates.dtlsSetup, mediaDescription.dtls);\n" +
            "            }\n" +
            "            mblock = fillTemplate(mblock, dtlsInfo);\n" +
            "\n" +
            "            var sctpInfo = {\"sctpmapLine\": \"\", \"fmt\": \"\"};\n" +
            "            if (mediaDescription.sctp) {\n" +
            "                addDefaults(mediaDescription.sctp, {\"streams\": null});\n" +
            "                sctpInfo.sctpmapLine = fillTemplate(templates.sctpmap, mediaDescription.sctp);\n" +
            "                sctpInfo.fmt = mediaDescription.sctp.port;\n" +
            "            }\n" +
            "            mblock = fillTemplate(mblock, sctpInfo);\n" +
            "\n" +
            "            sdpText += mblock;\n" +
            "        });\n" +
            "\n" +
            "        return sdpText;\n" +
            "    };\n" +
            "})();\n" +
            "\n" +
            "if (typeof(module) != \"undefined\" && typeof(exports) != \"undefined\")\n" +
            "    module.exports = SDP;\n";
}
