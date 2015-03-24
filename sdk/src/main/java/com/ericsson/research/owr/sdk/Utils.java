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

import com.ericsson.research.owr.AudioPayload;
import com.ericsson.research.owr.CodecType;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Payload;
import com.ericsson.research.owr.VideoPayload;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class Utils {
    private static final String TAG = "Utils";

    static List<Payload> transformPayloads(List<RtcPayload> payloads, MediaType mediaType) {
        if (payloads == null) {
            throw new NullPointerException("payloads should not be null");
        }
        if (mediaType == MediaType.UNKNOWN) {
            throw new IllegalArgumentException("media type should not be UNKNOWN");
        }
        List<Payload> result = new LinkedList<>();
        for (RtcPayload payload : payloads) {
            if ("RTX".equals(payload.getEncodingName())) {
                continue;
            }
            try {
                CodecType codecType = CodecType.valueOf(payload.getEncodingName().toUpperCase());
                RtcPayload rtxPayload = findRtxPayloadForPayloadType(payloads, payload.getPayloadType());

                Payload owrPayload;
                if (mediaType == MediaType.AUDIO) {
                    owrPayload = new AudioPayload(codecType, payload.getPayloadType(),
                            payload.getClockRate(), payload.getChannels());
                } else {
                    owrPayload = new VideoPayload(codecType, payload.getPayloadType(),
                            payload.getClockRate(), payload.isCcmFir(), payload.isNackPli());
                }
                if (rtxPayload != null) {
                    owrPayload.setRtxPayloadType(rtxPayload.getPayloadType());
                    Integer rtxTime = (Integer) rtxPayload.getParameters().get("rtx-time");
                    if (rtxTime != null) {
                        owrPayload.setRtxTime(rtxTime);
                    }
                }
                result.add(owrPayload);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "unknown codec type: " + payload.getEncodingName());
            }
        }
        return result;
    }

    private static RtcPayload findPayloadByPayloadType(List<RtcPayload> payloads, int payloadType) {
        if (payloadType < 0) {
            return null;
        }
        for (RtcPayload payload : payloads) {
            if (payload.getPayloadType() == payloadType) {
                return payload;
            }
        }
        return null;
    }

    private static RtcPayload findRtxPayloadForPayloadType(List<RtcPayload> payloads, int payloadType) {
        if (payloadType < 0) {
            return null;
        }
        for (RtcPayload payload : payloads) {
            if ("RTX".equals(payload.getEncodingName())) {
                int apt = getAssociatedPayloadType(payload);
                if (apt > 0 && apt == payloadType) {
                    return payload;
                }
            }
        }
        return null;
    }

    private static int getAssociatedPayloadType(RtcPayload payload) {
        Map<String, Object> parameters = payload.getParameters();
        if (parameters == null) {
            return -1;
        }
        try {
            return (int) parameters.get("apt");
        } catch (ClassCastException ignored) {
            return -1;
        } catch (NullPointerException ignored) {
            return -1;
        }
    }

    private static RtcPayload findMatchingPayload(List<RtcPayload> payloads, RtcPayload matchingPayload) {
        for (RtcPayload payload : payloads) {
            if (payload.getEncodingName() == null) {
                continue;
            }
            if (!payload.getEncodingName().equals(matchingPayload.getEncodingName())) {
                continue;
            }
            try {
                if ((int) payload.getParameters().get("packetization-mode") !=
                        (int) matchingPayload.getParameters().get("packetization-mode")) {
                    continue;
                }
            } catch (NullPointerException ignored) {
            }
            return payload;
        }
        return null;
    }

    private static RtcPayload intersectPayload(RtcPayload payload, RtcPayload filter) {
        return new PlainRtcPayload(
                payload.getPayloadType(),
                payload.getEncodingName(),
                payload.getClockRate(),
                payload.getParameters(),
                payload.getChannels(),
                payload.isNack() && filter.isNack(),
                payload.isNackPli() && filter.isNackPli(),
                payload.isCcmFir() && filter.isCcmFir()
        );
    }

    static List<RtcPayload> intersectPayloads(List<RtcPayload> payloads, List<RtcPayload> filterPayloads) {
        List<RtcPayload> result = new LinkedList<>();
        for (RtcPayload payload : payloads) {
            if ("RTX".equals(payload.getEncodingName())) {
                RtcPayload associatedPayload = findPayloadByPayloadType(payloads, getAssociatedPayloadType(payload));
                RtcPayload matchingPayload = findMatchingPayload(filterPayloads, associatedPayload);
                if (matchingPayload != null) {
                    if (findRtxPayloadForPayloadType(filterPayloads, matchingPayload.getPayloadType()) != null) {
                        result.add(payload);
                    }
                }
            } else {
                RtcPayload matchingPayload = findMatchingPayload(filterPayloads, payload);
                if (matchingPayload != null) {
                    result.add(intersectPayload(payload, matchingPayload));
                }
            }
        }
        return result;
    }
}
