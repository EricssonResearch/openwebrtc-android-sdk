/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.Collection;
import java.util.List;

public class SimpleRtcConfig extends RtcConfig {
    /**
     * Creates a configuration for setting up a basic audio/video call.
     *
     * @param sendAudio true if audio should be sent, audio may still be received
     * @param sendVideo true if video should be sent, video may still be received
     * @return a new RtcConfig with a simple audio/video call configuration
     */
    public static RtcConfig defaultConfig(boolean sendAudio, boolean sendVideo) {
        return null;
    }

    /**
     * Set the surface on which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surface The surface to render the self-view on, or null to disable the self-view.
     */
    public void setSelfView(Surface surface) {

    }

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surface The view to render the self-view in, or null to disable the self-view.
     */
    public void setSelfView(SurfaceView surface) {

    }

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surface The view to render the self-view in, or null to disable the self-view.
     */
    public void setSelfView(TextureView surface) {

    }

    /**
     * Set the surface on which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surface The surface to render the remote-view on, or null to disable the remote-view.
     */
    public void setRemoteView(Surface surface) {

    }

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surface The view to render the remote-view in, or null to disable the remote-view.
     */
    public void setRemoteView(SurfaceView surface) {

    }

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surface The view to render the remote-view in, or null to disable the remote-view.
     */
    public void setRemoteView(TextureView surface) {

    }

    @Override
    void setRemoteDescription(SessionDescription remoteDescription) {

    }

    @Override
    List<Stream> getStreams() {
        return null;
    }

    @Override
    Collection<HelperServer> getHelperServers() {
        return null;
    }
}
