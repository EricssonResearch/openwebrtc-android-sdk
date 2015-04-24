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

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.VideoRenderer;
import com.ericsson.research.owr.WindowRegistry;

public class VideoViewImpl implements VideoView, MediaSourceListener {
    private static final String TAG = "VideoViewImpl";

    private TextureViewTagger mTextureViewTagger;
    private VideoRenderer mVideoRenderer;
    private MediaSource mVideoSource;
    private final String mTag;
    private int mRotation;

    VideoViewImpl(MediaSourceProvider mediaSourceProvider, int width, int height, double framerate) {
        mVideoSource = null;
        mRotation = 0;

        mTag = Utils.randomString(32);

        mVideoRenderer = new VideoRenderer(mTag);
        mVideoRenderer.setRotation(mRotation);
        if (width > 0) {
            mVideoRenderer.setWidth(width);
        }
        if (height > 0) {
            mVideoRenderer.setHeight(height);
        }
        if (framerate > 0) {
            mVideoRenderer.setMaxFramerate(framerate);
        }
        mediaSourceProvider.addMediaSourceListener(this);
    }

    @Override
    public void setRotation(final int rotation) {
        if (rotation < 0 || rotation > 3) {
            throw new IllegalArgumentException(rotation + " is an invalid rotation, must be between 0 and 3");
        }
        mRotation = rotation;
        mVideoRenderer.setRotation(rotation);
    }

    @Override
    public int getRotation() {
        return mRotation;
    }

    @Override
    public synchronized void setMediaSource(final MediaSource mediaSource) {
        mVideoSource = mediaSource;
        mVideoRenderer.setSource(mediaSource);
    }

    private boolean viewIsActive() {
        return mTextureViewTagger != null;
    }

    public synchronized void setView(TextureView textureView) {
        if (textureView == null) {
            throw new NullPointerException("texture view may not be null");
        }
        if (!viewIsActive() && mVideoSource != null) {
            mVideoRenderer.setSource(mVideoSource);
        }
        stopViewTagger();
        mTextureViewTagger = new TextureViewTagger(mTag, textureView);
    }

    public synchronized void stop() {
        stopViewTagger();
        mVideoRenderer.setSource(null);
    }

    private void stopViewTagger() {
        if (mTextureViewTagger != null) {
            mTextureViewTagger.stop();
            mTextureViewTagger = null;
        }
    }

    private static class TextureViewTagger implements TextureView.SurfaceTextureListener {
        private final String mTag;
        private TextureView mTextureView;

        private TextureViewTagger(String tag, TextureView textureView) {
            mTag = tag;
            mTextureView = textureView;
            if (textureView.isAvailable()) {
                Surface surface = new Surface(textureView.getSurfaceTexture());
                WindowRegistry.get().register(mTag, surface);
            }
            mTextureView.setSurfaceTextureListener(this);
        }

        private synchronized void stop() {
            mTextureView.setSurfaceTextureListener(null);
            WindowRegistry.get().unregister(mTag);
        }

        @Override
        public synchronized void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            Surface surface = new Surface(surfaceTexture);
            WindowRegistry.get().register(mTag, surface);
        }

        @Override
        public synchronized boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            WindowRegistry.get().unregister(mTag);
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }
}