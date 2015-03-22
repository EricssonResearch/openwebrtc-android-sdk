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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class SessionDescriptionImpl implements SessionDescription {
    public static final String TAG = "SessionDescriptionImpl";

    private final ArrayList<StreamDescription> mStreamDescriptions;
    private final String mSessionId;
    private final Type mType;
    private Map<String, StreamDescription> mStreamDescriptionMap;

    SessionDescriptionImpl(Type type, String sessionId, Collection<StreamDescription> streamDescriptions) {
        mType = type;
        mSessionId = sessionId;
        mStreamDescriptions = new ArrayList<StreamDescription>(streamDescriptions.size());
        boolean createdMap = false;

        for (StreamDescription streamDescription : streamDescriptions) {
            mStreamDescriptions.add(streamDescription);
            String streamId = streamDescription.getId();
            if (streamId != null) {
                if (!createdMap) {
                    mStreamDescriptionMap = new HashMap<String, StreamDescription>();
                    createdMap = true;
                }
                mStreamDescriptionMap.put(streamId, streamDescription);
            }
        }
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public int getStreamDescriptionCount() {
        return mStreamDescriptions.size();
    }

    @Override
    public StreamDescription getStreamDescriptionByIndex(final int index) {
        return mStreamDescriptions.get(index);
    }

    @Override
    public StreamDescription getStreamDescriptionById(final String id) {
        if (id == null) {
            return null;
        }
        return mStreamDescriptionMap.get(id);
    }

    @Override
    public String getSessionId() {
        return mSessionId;
    }

    @Override
    public boolean hasStreamType(final StreamType streamType) {
        for (StreamDescription streamDescription : mStreamDescriptions) {
            if (streamDescription.getStreamType() == streamType) {
                return true;
            }
        }
        return false;
    }
}
