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

import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.Collection;
import java.util.List;

public class SimpleStreamSet extends StreamSet {
    /**
     * Creates a configuration for setting up a basic audio/video call.
     *
     * @param sendAudio true if audio should be sent, audio may still be received
     * @param sendVideo true if video should be sent, video may still be received
     * @return a new RtcConfig with a simple audio/video call configuration
     */
    public static StreamSet defaultConfig(boolean sendAudio, boolean sendVideo) {
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
}
