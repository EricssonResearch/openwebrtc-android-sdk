/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
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
