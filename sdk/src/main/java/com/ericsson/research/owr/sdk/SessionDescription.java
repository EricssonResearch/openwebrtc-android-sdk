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
    public DescriptionType getDescriptionType();

    /**
     * @return the number of media descriptions available
     */
    public int getMediaDescriptionCount();

    /**
     * Returns the media description at index
     * @param index the index of the media description to get
     * @return a MediaDescription
     * @throws java.lang.IndexOutOfBoundsException if index < 0 or index >= count
     */
    public StreamDescription getMediaDescriptionByIndex(int index);

    /**
     * Returns a media description by id, or null if none is found.
     * Always returns null if null is passed as id.
     * @param id the id of the media description to get
     * @return a MediaDescription, or null if none is found
     */
    public StreamDescription getMediaDescriptionById(String id);

    /**
     * @return the id of the session
     */
    public long getSessionId();

    /**
     * @param streamType a stream type
     * @return true if the session description contains a stream of type streamType, false otherwise
     */
    public boolean hasStreamType(StreamType streamType);
}
