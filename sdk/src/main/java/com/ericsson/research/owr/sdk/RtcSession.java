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
 * An interface representing a RTC session between two peers
 */
public interface RtcSession {

    /**
     * Sets the listener that will be called when a local candidate is generated.
     * @param listener the listener
     */
    void setOnLocalCandidateListener(OnLocalCandidateListener listener);

    /**
     * Sets the listener that will be called once a local description is ready to be sent to the peer.
     * @param listener the listener
     */
    void setOnLocalDescriptionListener(OnLocalDescriptionListener listener);

    /**
     * Start the session by getting ready to receive media, and generate a local description for the session.
     * If the remote description has already been set then media will sent as well.
     * Start can not  be called twice in a row without calling stop inbetween.
     * @param streamSet the stream set that should be used for the call
     */
    void start(StreamSet streamSet);

    /**
     * Sets the remote description of the session. This method should only be called once, and only after
     * setup has been called for outbound calls, and before setup for inbound calls.
     *
     * @param remoteDescription the SessionDescription received form the other peer.
     */
    void setRemoteDescription(SessionDescription remoteDescription) throws InvalidDescriptionException;

    /**
     * Add a RtcCandidate that is received form the other peer.
     *
     * @param candidate the candidate to add.
     */
    void addRemoteCandidate(RtcCandidate candidate);

    /**
     * Ends the call, this has no effect if the session isn't active.
     */
    void stop();

    /**
     * Dumps the current pipeline graph in dot format.
     * @return the pipeline graph in dot format
     */
    String dumpPipelineGraph();

    interface OnLocalCandidateListener {
        /**
         * Called when a local candidate is generated.
         * @param candidate a local RtcCandidate that should be sent to the other peer.
         */
        void onLocalCandidate(RtcCandidate candidate);
    }

    interface OnLocalDescriptionListener {
        /**
         * Called once the local description is ready to be sent to the peer.
         * @param localDescription a SessionDescription that should be sent to the other peer.
         */
        void onLocalDescription(SessionDescription localDescription);
    }
}
