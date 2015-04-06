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

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.ericsson.research.owr.AudioRenderer;
import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Owr;
import com.ericsson.research.owr.VideoRenderer;
import com.ericsson.research.owr.WindowRegistry;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleStreamSet extends StreamSet {
    private static final String TAG = "SimpleStreamSet";

    private final boolean mWantVideo;
    private final boolean mWantAudio;

    private final String mSelfViewTag;
    private final String mRemoteViewTag;

    private SurfaceViewTagger mSelfViewSurfaceTagger;
    private TextureViewTagger mSelfViewTextureTagger;
    private SurfaceViewTagger mRemoteViewSurfaceTagger;
    private TextureViewTagger mRemoteViewTextureTagger;

    private final VideoRenderer mRemoteViewRenderer;
    private final VideoRenderer mSelfViewRenderer;
    private final AudioRenderer mAudioRenderer;

    private final LinkedList<MediaSource> mAudioSources;
    private final LinkedList<MediaSource> mVideoSources;
    private MediaSourceDelegate mVideoSourceDelegate;
    private MediaSourceDelegate mAudioSourceDelegate;

    private SimpleStreamSet(boolean sendAudio, boolean sendVideo) {
        mSelfViewTag = Utils.randomString(32);
        mRemoteViewTag = Utils.randomString(32);
        mWantAudio = sendAudio;
        mWantVideo = sendVideo;

        mAudioRenderer = new AudioRenderer();

        mRemoteViewRenderer = new VideoRenderer(mRemoteViewTag);
        mRemoteViewRenderer.setWidth(512);
        mRemoteViewRenderer.setHeight(512);
        mRemoteViewRenderer.setMaxFramerate(30);

        if (mWantVideo) {
            mSelfViewRenderer = new VideoRenderer(mSelfViewTag);
            mSelfViewRenderer.setWidth(512);
            mSelfViewRenderer.setHeight(512);
            mSelfViewRenderer.setMaxFramerate(30);
        } else {
            mSelfViewRenderer = null;
        }

        EnumSet<MediaType> mediaTypes = EnumSet.noneOf(MediaType.class);
        if (mWantAudio) {
            mediaTypes.add(MediaType.AUDIO);
        }
        if (mWantVideo) {
            mediaTypes.add(MediaType.VIDEO);
        }
        mAudioSources = new LinkedList<>();
        mVideoSources = new LinkedList<>();
        Owr.getCaptureSources(mediaTypes, new CaptureSourcesCallback() {
            @Override
            public void onCaptureSourcesCallback(final List<MediaSource> mediaSources) {
                synchronized (SimpleStreamSet.this) {
                    for (MediaSource mediaSource : mediaSources) {
                        if (mediaSource.getMediaType().contains(MediaType.AUDIO)) {
                            mAudioSources.add(mediaSource);
                        } else {
                            mVideoSources.add(mediaSource);
                        }
                    }
                    boolean haveSelfView = mSelfViewSurfaceTagger != null || mSelfViewTextureTagger != null;
                    if (mWantVideo && !mVideoSources.isEmpty() && haveSelfView) {
                        mSelfViewRenderer.setSource(mVideoSources.get(0));
                    }
                    if (mVideoSourceDelegate != null && !mVideoSources.isEmpty()) {
                        mVideoSourceDelegate.setMediaSource(mVideoSources.getFirst());
                    }
                    if (mAudioSourceDelegate != null && !mAudioSources.isEmpty()) {
                        mAudioSourceDelegate.setMediaSource(mAudioSources.getFirst());
                    }
                }
            }
        });
    }

    /**
     * Creates a configuration for setting up a basic audio/video call.
     *
     * @param sendAudio true if audio should be sent, audio may still be received
     * @param sendVideo true if video should be sent, video may still be received
     * @return a new RtcConfig with a simple audio/video call configuration
     */
    public static SimpleStreamSet defaultConfig(boolean sendAudio, boolean sendVideo) {
        return new SimpleStreamSet(sendAudio, sendVideo);
    }

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param surfaceView The view to render the self-view in, or null to disable the self-view.
     */
    public synchronized void setSelfView(SurfaceView surfaceView) {
        if (!mWantVideo) {
            return;
        }
        stopSelfViewTaggers();
        if (surfaceView != null) {
            if (!mVideoSources.isEmpty()) {
                mSelfViewRenderer.setSource(mVideoSources.get(0));
            }
            mSelfViewSurfaceTagger = new SurfaceViewTagger(mSelfViewTag, surfaceView);
        }
    }

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param textureView The view to render the self-view in, or null to disable the self-view.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public synchronized void setSelfView(TextureView textureView) {
        if (!mWantVideo) {
            return;
        }
        stopSelfViewTaggers();
        if (textureView != null) {
            if (!mVideoSources.isEmpty()) {
                mSelfViewRenderer.setSource(mVideoSources.get(0));
            }
            mSelfViewTextureTagger = new TextureViewTagger(mSelfViewTag, textureView);
        }
    }

    /**
     * Stops self-view rendering.
     * This should always be called in order to release resources.
     */
    public synchronized void stopSelfView() {
        stopSelfViewTaggers();
        if (mSelfViewRenderer != null) {
            mSelfViewRenderer.setSource(null);
        }
    }

    private void stopSelfViewTaggers() {
        if (mSelfViewTextureTagger != null) {
            mSelfViewTextureTagger.stop();
            mSelfViewTextureTagger = null;
        }
        if (mSelfViewSurfaceTagger != null) {
            mSelfViewSurfaceTagger.stop();
            mSelfViewSurfaceTagger = null;
        }
    }

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param surfaceView The view to render the remote-view in, or null to disable the remote-view.
     */
    public synchronized void setRemoteView(SurfaceView surfaceView) {
        stopRemoteViewTaggers();
        if (surfaceView != null) {
            mRemoteViewSurfaceTagger = new SurfaceViewTagger(mRemoteViewTag, surfaceView);
        }
    }

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param textureView The view to render the remote-view in, or null to disable the remote-view.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public synchronized void setRemoteView(TextureView textureView) {
        stopRemoteViewTaggers();
        if (textureView != null) {
            mRemoteViewTextureTagger = new TextureViewTagger(mRemoteViewTag, textureView);
        }
    }

    /**
     * Stops remote-view rendering.
     */
    public synchronized void stopRemoteView() {
        stopRemoteViewTaggers();
    }

    private void stopRemoteViewTaggers() {
        if (mRemoteViewTextureTagger != null) {
            mRemoteViewTextureTagger.stop();
            mRemoteViewTextureTagger = null;
        }
        if (mRemoteViewSurfaceTagger != null) {
            mRemoteViewSurfaceTagger.stop();
            mRemoteViewSurfaceTagger = null;
        }
    }

    @Override
    protected List<? extends Stream> getStreams() {
        return Arrays.asList(new SimpleMediaStream(false), new SimpleMediaStream(true));
    }

    /**
     * Dumps the current pipeline graph in dot format.
     * Each key-value pair in the returned map is a pipeline name and a graph.
     * @return a map of pipeline graphs
     */
    public Map<String, String> dumpPipelineGraphs() {
        HashMap<String, String> result = new HashMap<>();
        if (!mVideoSources.isEmpty()) {
            result.put("video-source", mVideoSources.getFirst().getDotData());
        }
        if (!mAudioSources.isEmpty()) {
            result.put("audio-source", mAudioSources.getFirst().getDotData());
        }
        if (mAudioRenderer != null) {
            result.put("remote-audio-renderer", mAudioRenderer.getDotData());
        }
        if (mRemoteViewRenderer != null) {
            result.put("remote-video-renderer", mRemoteViewRenderer.getDotData());
        }
        if (mSelfViewRenderer != null) {
            result.put("local-video-renderer", mSelfViewRenderer.getDotData());
        }
        return result;
    }

    private class SimpleMediaStream extends MediaStream {
        private final String mId;
        private final boolean mIsVideo;

        private SimpleMediaStream(boolean isVideo) {
            mIsVideo = isVideo;
            mId = Utils.randomString(27);
        }

        @Override
        protected String getId() {
            return mId;
        }

        @Override
        protected MediaType getMediaType() {
            return mIsVideo ? MediaType.VIDEO : MediaType.AUDIO;
        }

        @Override
        protected boolean wantSend() {
            return mIsVideo ? mWantVideo : mWantAudio;
        }

        @Override
        protected boolean wantReceive() {
            return true;
        }

        @Override
        protected void onRemoteMediaSource(final MediaSource mediaSource) {
            if (mIsVideo) {
                mRemoteViewRenderer.setSource(mediaSource);
            } else {
                mAudioRenderer.setSource(mediaSource);
            }
        }

        @Override
        protected void setMediaSourceDelegate(final MediaSourceDelegate mediaSourceDelegate) {
            synchronized (SimpleStreamSet.this) {
                if (mIsVideo) {
                    mVideoSourceDelegate = mediaSourceDelegate;
                    if (mediaSourceDelegate != null) {
                        if (!mVideoSources.isEmpty()) {
                            mVideoSourceDelegate.setMediaSource(mVideoSources.getFirst());
                        }
                    }
                } else {
                    if (mediaSourceDelegate != null) {
                        mAudioSourceDelegate = mediaSourceDelegate;
                        if (!mAudioSources.isEmpty()) {
                            mAudioSourceDelegate.setMediaSource(mAudioSources.getFirst());
                        }
                    }
                }
            }
        }

        @Override
        public void setStreamMode(final StreamMode mode) {
            Log.i(TAG, (mIsVideo ? "video" : "audio") + " stream mode set: " + mode.name());
        }
    }

    private class SurfaceViewTagger implements SurfaceHolder.Callback {
        private final String mTag;
        private SurfaceView mSurfaceView;

        private SurfaceViewTagger(String tag, SurfaceView surfaceView) {
            mTag = tag;
            mSurfaceView = surfaceView;
            mSurfaceView.getHolder().addCallback(this);
        }

        private void stop() {
            mSurfaceView.getHolder().removeCallback(this);
            WindowRegistry.get().unregister(mTag);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            WindowRegistry.get().register(mTag, holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            WindowRegistry.get().unregister(mTag);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private class TextureViewTagger implements TextureView.SurfaceTextureListener {
        private final String mTag;
        private TextureView mTextureView;

        private TextureViewTagger(String tag, TextureView textureView) {
            mTag = tag;
            mTextureView = textureView;
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
