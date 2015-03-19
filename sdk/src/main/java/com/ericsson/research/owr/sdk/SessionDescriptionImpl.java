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
    private final long mSessionId;
    private final DescriptionType mType;
    private Map<String, StreamDescription> mStreamDescriptionMap;

    SessionDescriptionImpl(DescriptionType type, long sessionId, Collection<StreamDescription> streamDescriptions) {
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
    public DescriptionType getDescriptionType() {
        return mType;
    }

    @Override
    public int getMediaDescriptionCount() {
        return mStreamDescriptions.size();
    }

    @Override
    public StreamDescription getMediaDescriptionByIndex(final int index) {
        return mStreamDescriptions.get(index);
    }

    @Override
    public StreamDescription getMediaDescriptionById(final String id) {
        if (id == null) {
            return null;
        }
        return mStreamDescriptionMap.get(id);
    }

    @Override
    public long getSessionId() {
        return mSessionId;
    }
}
