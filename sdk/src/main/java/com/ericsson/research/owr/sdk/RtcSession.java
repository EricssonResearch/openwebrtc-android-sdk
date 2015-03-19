/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

/**
 * An interface representing a RTC session between two peers
 */
public interface RtcSession {
    /**
     * Set the surface on which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surface The surface to render the self-view on, or null to disable the self-view.
     */
    public void setSelfView(Surface surface);

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surface The view to render the self-view in, or null to disable the self-view.
     */
    public void setSelfView(SurfaceView surface);

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surface The view to render the self-view in, or null to disable the self-view.
     */
    public void setSelfView(TextureView surface);

    /**
     * Set the surface on which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surface The surface to render the remote-view on, or null to disable the remote-view.
     */
    public void setRemoteView(Surface surface);

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surface The view to render the remote-view in, or null to disable the remote-view.
     */
    public void setRemoteView(SurfaceView surface);

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surface The view to render the remote-view in, or null to disable the remote-view.
     */
    public void setRemoteView(TextureView surface);

    /**
     * Sets the listener that will be called when a local candidate is generated.
     * @param listener the listener that will be called when a local candidate is available.
     */
    public void setOnLocalCandidateListener(OnLocalCandidateListener listener);

    /**
     * Initiate the session and get ready to receive media and generate a SessionDescription for the other peer.
     *
     * @param sendAudio true if audio should be sent, audio may still be received
     * @param sendVideo true if video should be sent, video may still be received
     * @param callback a callback that is called with a SessionDescription that should be sent to the other peer.
     */
    public void setup(boolean sendAudio, boolean sendVideo, SetupCompleteCallback callback);

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
