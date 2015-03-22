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

import android.util.Pair;

import com.ericsson.research.owr.HelperServerType;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Payload;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An interface masquerading as an abstract class that allows low-level configuration
 * of an RTC session.
 * The reason it's an abstract class is to be able to make the abstract methods package-private
 */
public abstract class RtcConfig {
    /**
     * An interface that represents a single audio, video or data stream.
     * The stream interface Stream should not be implemented directly, but through the MediaStream and DataStream interfaces.
     */
    abstract class Stream {
        /** Implementations should return true if the given fingerprint hash function is allowed
         *
         * @param hashFunction the name of the hash function, e.g. sha-256
         * @return false if the hash function should be discarded, true othersize
         */
        abstract boolean allowFingerprintHashFunction(String hashFunction);

        /**
         * Implementations should return a list of default hash functions, ordered from most-preferred to least-preferred
         * Typically sha-256, sha-512, or sha-1
         * @return a list of hash functions to be used
         */
        abstract List<String> getDefaultFingerprintHashFunctions();
    }

    /**
     * An interface that represents a single audio or video stream.
     */
    abstract class MediaStream extends Stream {
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
         * Implementations should return a list of payloads, ordered by most-preferred to least-preferred.
         * The payload type should be the same as the media type of the stream.
         * @return a list of payload + parameters pairs, the parameter map may be null
         */
        abstract List<Pair<Payload, Map<String, Object>>> getDefaultPayloads();

        /**
         * Implementations should return true if the given payload/parameter pair is supported.
         * The payload or parameters should NOT be modified.
         * @param payload an audio or video payload
         * @param parameters parameters to the given payload
         * @return false if the payload should be discarded, false otherwise
         */
        abstract boolean supportsPayload(Payload payload, Map<String, Object> parameters);
    }

    /**
     * Once the remote description is receive it should be used for any further configuration.
     * @param remoteDescription a SessionDescription received from the remote peer.
     */
    abstract void setRemoteDescription(SessionDescription remoteDescription);

    /**
     * Implementations should return a list of streams that are sent and/or received.
     * The list needs to match any remote description that might have been set.
     * @return a list of streams
     */
    abstract List<Stream> getStreams();

    /**
     * Implementations should return a list of helper servers that are used for ICE.
     * @return a collection of helper servers
     */
    abstract Collection<HelperServer> getHelperServers();


    public static class HelperServer {
        private final HelperServerType mType;
        private final String mAddress;
        private final int mPort;
        private final String mUsername;
        private final String mPassword;

        public HelperServer(HelperServerType type, String address, int port, String username, String password) {
            mType = type;
            mAddress = address;
            mPort = port;
            mUsername = username;
            mPassword = password;
        }

        public HelperServerType getType() {
            return mType;
        }

        public String getAddress() {
            return mAddress;
        }

        public int getPort() {
            return mPort;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getPassword() {
            return mPassword;
        }
    }
}
