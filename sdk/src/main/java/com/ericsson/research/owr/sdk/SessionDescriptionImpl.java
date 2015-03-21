/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
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
