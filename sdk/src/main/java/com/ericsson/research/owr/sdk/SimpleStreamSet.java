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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.ericsson.research.owr.AudioRenderer;
import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.MediaRenderer;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimpleStreamSet extends StreamSet {
    private static final String TAG = "SimpleStreamSet";

    private static final long SOURCE_CALLBACK_TIMEOUT_MS = 2000;
    private static final long CAMERA_CLOSE_DURATION_MS = 1000;
    private static final long CAMERA_OPEN_DURATION_MS = 8000;

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

    private MediaSource mAudioSource = null;
    private final LinkedList<MediaSource> mVideoSources = new LinkedList<>();
    private MediaSourceDelegate mVideoSourceDelegate;
    private MediaSourceDelegate mAudioSourceDelegate;

    private int mVideoSourceIndex;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mActiveVideoSourceIndex;

    private VideoSourceState mVideoSourceState;

    public enum VideoSourceState {
        READY,
        CLOSING,
        OPENING;

        public boolean canSetSource() {
            return this == READY;
        }
    }

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

        mSelfViewRenderer = new VideoRenderer(mSelfViewTag);
        mSelfViewRenderer.setWidth(512);
        mSelfViewRenderer.setHeight(512);
        mSelfViewRenderer.setMaxFramerate(30);

        mVideoSourceIndex = 0;
        mActiveVideoSourceIndex = 0;
        mVideoSourceState = VideoSourceState.READY;

        final CountDownLatch latch = new CountDownLatch(1);
        Owr.getCaptureSources(EnumSet.of(MediaType.AUDIO, MediaType.VIDEO), new CaptureSourcesCallback() {
            @Override
            public void onCaptureSourcesCallback(final List<MediaSource> mediaSources) {
                for (MediaSource mediaSource : mediaSources) {
                    if (mediaSource.getMediaType().contains(MediaType.AUDIO)) {
                        if (mAudioSource != null) {
                            Log.e(TAG, "found a seconds audio source: previous one was " + mAudioSource.getName() + ", new one is " + mediaSource.getName() + ", ignoring");
                        } else {
                            mAudioSource = mediaSource;
                        }
                    } else {
                        mVideoSources.add(mediaSource);
                    }
                }
                latch.countDown();
            }
        });

        try {
            latch.await(SOURCE_CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkIndex(String name, int index, int count) {
        if (index >= count) {
            throw new IndexOutOfBoundsException("invalid " + name + " index, " + index + " >= " + count);
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("invalid " + name + " index, " + index + " < 0 ");
        }
    }

    public int getVideoSourceCount() {
        return mVideoSources.size();
    }

    public synchronized void setVideoSourceIndex(final int index) {
        checkIndex("video source", index, getVideoSourceCount());
        mVideoSourceIndex = index;
        if (mActiveVideoSourceIndex == index) {
            return;
        }

        if (!mVideoSourceState.canSetSource()) {
            Log.d(TAG, "could not set video source index directly in state " + mVideoSourceState + ", queueing switch");
            return;
        }

        mSelfViewRenderer.setSource(null);
        if (mVideoSourceDelegate != null) {
            mVideoSourceDelegate.setMediaSource(null);
        }
        mVideoSourceState = VideoSourceState.CLOSING;

        final Runnable onCameraOpened = new Runnable() {
            @Override
            public void run() {
                synchronized (SimpleStreamSet.this) {
                    mVideoSourceState = VideoSourceState.READY;
                    if (mActiveVideoSourceIndex != mVideoSourceIndex) {
                        setVideoSourceIndex(mVideoSourceIndex);
                    }
                }
            }
        };

        final Runnable onCameraClosed = new Runnable() {
            @Override
            public void run() {
                synchronized (SimpleStreamSet.this) {
                    mActiveVideoSourceIndex = mVideoSourceIndex;
                    MediaSource videoSource = getSelectedVideoSource();
                    if (mVideoSourceDelegate != null) {
                        mVideoSourceDelegate.setMediaSource(videoSource);
                    }
                    if (selfViewIsActive()) {
                        mSelfViewRenderer.setSource(videoSource);
                    }
                    mVideoSourceState = VideoSourceState.OPENING;
                    mHandler.postDelayed(onCameraOpened, CAMERA_OPEN_DURATION_MS);
                }
            }
        };

        mHandler.postDelayed(onCameraClosed, CAMERA_CLOSE_DURATION_MS);
    }

    VideoSourceState getVideoSourceState() {
        return mVideoSourceState;
    }

    public int getActiveVideoSourceIndex() {
        return mActiveVideoSourceIndex;
    }

    public int getVideoSourceIndex() {
        return mVideoSourceIndex;
    }

    private MediaSource getSelectedVideoSource() {
        if (!mVideoSources.isEmpty()) {
            return mVideoSources.get(mVideoSourceIndex);
        }
        return null;
    }

    private boolean selfViewIsActive() {
        return mSelfViewSurfaceTagger != null || mSelfViewTextureTagger != null;
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
        if (surfaceView == null) {
            throw new NullPointerException("surface view may not be null");
        }
        if (!selfViewIsActive() && mVideoSourceState.canSetSource()) {
            mSelfViewRenderer.setSource(getSelectedVideoSource());
        }
        stopSelfViewTaggers();
        mSelfViewSurfaceTagger = new SurfaceViewTagger(mSelfViewTag, surfaceView);
    }

    /**
     * Set the view in which the self-view should be rendered.
     * If a self-view is set it will be rendered even if video is not sent to the other peer.
     *
     * @param textureView The view to render the self-view in, or null to disable the self-view.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public synchronized void setSelfView(TextureView textureView) {
        if (textureView == null) {
            throw new NullPointerException("texture view may not be null");
        }
        if (!selfViewIsActive() && mVideoSourceState.canSetSource()) {
            mSelfViewRenderer.setSource(getSelectedVideoSource());
        }
        stopSelfViewTaggers();
        mSelfViewTextureTagger = new TextureViewTagger(mSelfViewTag, textureView);
    }

    /**
     * Stops self-view rendering, it will not stop video from being sent.
     * This should always be called in order to release resources.
     */
    public synchronized void stopSelfView() {
        stopSelfViewTaggers();
        mSelfViewRenderer.setSource(null);
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
        if (surfaceView == null) {
            throw new NullPointerException("surface view may not be null");
        }
        stopRemoteViewTaggers();
        mRemoteViewSurfaceTagger = new SurfaceViewTagger(mRemoteViewTag, surfaceView);
    }

    /**
     * Set the view in which the remote-view should be rendered.
     * The remote-view will only be rendered if the peer is streaming video.
     *
     * @param textureView The view to render the remote-view in, or null to disable the remote-view.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public synchronized void setRemoteView(TextureView textureView) {
        if (textureView == null) {
            throw new NullPointerException("texture view may not be null");
        }
        stopRemoteViewTaggers();
        mRemoteViewTextureTagger = new TextureViewTagger(mRemoteViewTag, textureView);
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
        if (mAudioSource != null) {
            result.put("audio-source", mAudioSource.getDotData());
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
            MediaRenderer renderer = mIsVideo ? mRemoteViewRenderer : mAudioRenderer;
            renderer.setSource(mediaSource);
            renderer.setDisabled(mediaSource == null);
        }

        @Override
        protected void setMediaSourceDelegate(final MediaSourceDelegate mediaSourceDelegate) {
            synchronized (SimpleStreamSet.this) {
                if (mIsVideo) {
                    mVideoSourceDelegate = mediaSourceDelegate;
                    if (mediaSourceDelegate != null) {
                        if (mVideoSourceState.canSetSource()) {
                            mVideoSourceDelegate.setMediaSource(getSelectedVideoSource());
                        }
                    }
                } else {
                    mAudioSourceDelegate = mediaSourceDelegate;
                    if (mediaSourceDelegate != null) {
                        mAudioSourceDelegate.setMediaSource(mAudioSource);
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
