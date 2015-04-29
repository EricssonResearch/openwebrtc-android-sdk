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

import android.view.TextureView;

public interface VideoView {
    /**
     * Sets the rotation of the video in multiples of 90 degrees
     * @param rotation 0, 1, 2, or 3, any other value will throw an IllegalArgumentException
     */
    void setRotation(int rotation);

    /**
     * @return current rotation in multiples of 90 degrees
     */
    int getRotation();

    /**
     * Set whether or not the video should be mirrored.
     * @param mirrored true if the rendered video should be mirrored, false otherwise.
     */
    void setMirrored(boolean mirrored);

    /**
     * @return true if the rendered video will be mirrored, false otherwise.
     */
    boolean isMirrored();

    /**
     * Set the view in which the video should be rendered, and starts the view if it was stopped.
     *
     * @param view The view to render the video in, may not be null.
     */
    void setView(TextureView view);

    /**
     * Stops the video view and frees all resources. Calling setView again will resume the view.
     * Depending on the type of the source it might be required to call this function in order
     * to not leak resources. Calling this function even if it isn't needed is never an error.
     */
    void stop();
}
