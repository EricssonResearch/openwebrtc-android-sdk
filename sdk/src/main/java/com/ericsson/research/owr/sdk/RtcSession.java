/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

/**
 * An interface representing a RTC session between two peers
 */
public interface RtcSession {

    /**
     * Sets the listener that will be called when a local candidate is generated.
     * @param listener the listener that will be called when a local candidate is available.
     */
    public void setOnLocalCandidateListener(OnLocalCandidateListener listener);

    /**
     * Initiate the session and get ready to receive media and generate a SessionDescription for the other peer.
     *
     * @param config the configuration that should be used for the call
     * @param callback a callback that is called with a SessionDescription that should be sent to the other peer.
     */
    public void setup(RtcConfig config, SetupCompleteCallback callback);

    /**
     * If the session is an outbound call, the answer needs to be provided once it's received from the other peer.
     *
     * @param remoteDescription the SessionDescription received form the other peer.
     */
    public void provideAnswer(SessionDescription remoteDescription);

    /**
     * Add a RtcCandidate that is received form the other peer.
     *
     * @param candidate the candidate to add.
     */
    public void addRemoteCandidate(RtcCandidate candidate);

    /**
     * End the call
     */
    public void end();

    public interface OnLocalCandidateListener {
        /**
         * Called when a local candidate is generated.
         * @param candidate a local RtcCandidate that should be sent to the other peer.
         */
        public void onLocalCandidate(RtcCandidate candidate);
    }

    public interface SetupCompleteCallback {
        /**
         * Called once the call setup is complete.
         * @param localDescription a SessionDescription that should be sent to the other peer.
         */
        public void onSetupComplete(SessionDescription localDescription);
    }
}
