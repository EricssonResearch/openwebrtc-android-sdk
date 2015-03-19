/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import com.ericsson.research.owr.MediaType;

public enum StreamType {
    AUDIO(MediaType.AUDIO), VIDEO(MediaType.VIDEO), DATA(MediaType.UNKNOWN);

    private final MediaType mMediaType;

    private StreamType(MediaType mediaType) {
        mMediaType = mediaType;
    }

    public MediaType getMediaType() {
        return mMediaType;
    }
}
