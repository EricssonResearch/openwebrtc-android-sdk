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

/**
 * An interface for an immutable description of RTC session at different stages.
 */
public interface SessionDescription {
    /**
     * @return the type of the session description
     */
    public Type getType();

    /**
     * @return the number of media descriptions available
     */
    public int getStreamDescriptionCount();

    /**
     * Returns the media description at index
     * @param index the index of the media description to get
     * @return a MediaDescription
     * @throws java.lang.IndexOutOfBoundsException if index < 0 or index >= count
     */
    public StreamDescription getStreamDescriptionByIndex(int index);

    /**
     * Returns a media description by id, or null if none is found.
     * Always returns null if null is passed as id.
     * @param id the id of the media description to get
     * @return a MediaDescription, or null if none is found
     */
    public StreamDescription getStreamDescriptionById(String id);

    /**
     * @return the id of the session
     */
    public String getSessionId();

    /**
     * @param streamType a stream type
     * @return true if the session description contains a stream of type streamType, false otherwise
     */
    public boolean hasStreamType(StreamType streamType);

    public enum Type {
        OFFER, ANSWER
    }
}
