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

import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;

import java.util.List;

/**
 * An interface masquerading as an abstract class that represents a set of stream to attach to a RtcSession
 * The reason it's an abstract class is to be able to make the abstract methods package-private
 */
public abstract class StreamSet {
    /**
     * An interface that represents a single audio, video or data stream.
     * The stream interface Stream should not be implemented directly, but through the MediaStream and DataStream interfaces.
     */
    interface Stream {}

    /**
     * An interface that represents a single audio or video stream.
     */
    abstract class MediaStream implements Stream {
        /**
         * Implementations should return a unique identifier for the stream, or null.
         * @return a unique identifier, or null
         */
        abstract String getId();

        /**
         * Implementations should return the media type of the stream.
         * @return the media type of the stream
         */
        abstract MediaType getMediaType();

        /**
         * If the implementation returns false no local stream will be sent, event if one is requested by the peer.
         * @return false if no media should be sent, true otherwise
         */
        abstract boolean wantSend();

        /**
         * If the implementation return false no remote stream will be requested
         * A remote stream might still be received though, in which case it can then be ignored.
         * @return true if remote media should be requested, false otherwise
         */
        abstract boolean wantReceive();

        /**
         * This method is called when the media source for the stream is received
         * @param mediaSource a media source matching the media type of the stream
         */
        abstract void onRemoteMediaSource(MediaSource mediaSource);

        /**
         * Implementations should return the media source for the stream.
         * The media source type type should be the same as the media type of the stream.
         * @return a media source matching the media type of the stream
         */
        abstract MediaSource getMediaSource();

        /**
         * Called once the final mode has been determined for the stream
         * @param mode of the stream
         */
        abstract void setStreamMode(StreamDescription.Mode mode);
    }

    /**
     * Implementations should return a list of streams that are sent and/or received.
     * The list needs to match any remote description that might have been set.
     * @return a list of streams
     */
    abstract List<Stream> getStreams();
}
