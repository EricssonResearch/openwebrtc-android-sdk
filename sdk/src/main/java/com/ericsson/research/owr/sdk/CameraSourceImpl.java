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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Owr;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class CameraSourceImpl extends CameraSource {
    private static final String TAG = "LocalMediaSourceImpl";

    private static final long SOURCE_CALLBACK_TIMEOUT_MS = 1000;
    private static final long CAMERA_CLOSE_DURATION_MS = 1000;
    private static final long CAMERA_OPEN_DURATION_MS = 8000;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private MediaSourceListenerSet mListeners = new MediaSourceListenerSet();

    private final List<MediaSource> mVideoSources;
    private int mVideoSourceIndex;
    private int mActiveVideoSourceIndex;

    private VideoSourceState mVideoSourceState;

    private CameraSourceImpl(List<MediaSource> videoSources) {
        mVideoSources = videoSources;
        mVideoSourceState = VideoSourceState.READY;

        mVideoSourceIndex = 0;
        mActiveVideoSourceIndex = 0;
        mListeners.notifyListeners(getSelectedMediaSource());
    }

    @Override
    public int getCount() {
        return mVideoSources.size();
    }

    @Override
    public String getName(final int index) {
        checkIndex(index);
        return mVideoSources.get(index).getName();
    }

    private void checkIndex(int index) {
        int count = getCount();
        if (index >= count) {
            throw new IndexOutOfBoundsException("invalid camera index, " + index + " >= " + count);
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("invalid camera index, " + index + " < 0 ");
        }
    }

    @Override
    public List<String> dumpPipelineGraphs() {
        List<String> result = new ArrayList<>();
        for (MediaSource videoSource : mVideoSources) {
            result.add(videoSource.getDotData());
        }
        return result;
    }

    @Override
    public synchronized void selectSource(final int index) {
        checkIndex(index);
        mVideoSourceIndex = index;
        if (mActiveVideoSourceIndex == index) {
            return;
        }

        if (mVideoSourceState != VideoSourceState.READY) {
            Log.d(TAG, "could not set video source index directly in state " + mVideoSourceState + ", queueing switch");
            return;
        }

        mListeners.notifyListeners(null);
        mVideoSourceState = VideoSourceState.CLOSING;

        final Runnable onCameraOpened = new Runnable() {
            @Override
            public void run() {
                synchronized (CameraSourceImpl.this) {
                    mVideoSourceState = VideoSourceState.READY;
                    if (mActiveVideoSourceIndex != mVideoSourceIndex) {
                        selectSource(mVideoSourceIndex);
                    }
                }
            }
        };

        final Runnable onCameraClosed = new Runnable() {
            @Override
            public void run() {
                synchronized (CameraSourceImpl.this) {
                    mActiveVideoSourceIndex = mVideoSourceIndex;
                    MediaSource videoSource = getSelectedMediaSource();
                    mListeners.notifyListeners(videoSource);
                    mVideoSourceState = VideoSourceState.OPENING;
                    mHandler.postDelayed(onCameraOpened, CAMERA_OPEN_DURATION_MS);
                }
            }
        };

        mHandler.postDelayed(onCameraClosed, CAMERA_CLOSE_DURATION_MS);
    }

    @Override
    public synchronized void addMediaSourceListener(final MediaSourceListener listener) {
        mListeners.addListener(listener);
    }

    public enum VideoSourceState {
        READY, CLOSING, OPENING
    }

    VideoSourceState getVideoSourceState() {
        return mVideoSourceState;
    }

    public int getActiveSource() {
        return mActiveVideoSourceIndex;
    }

    public int getSelectedSource() {
        return mVideoSourceIndex;
    }

    private MediaSource getSelectedMediaSource() {
        if (!mVideoSources.isEmpty()) {
            return mVideoSources.get(mVideoSourceIndex);
        }
        return null;
    }

    public static CameraSourceImpl create() {
        final List<MediaSource> videoSources = new ArrayList<>();

        final CountDownLatch latch = new CountDownLatch(1);
        Owr.getCaptureSources(EnumSet.of(MediaType.VIDEO), new CaptureSourcesCallback() {
            @Override
            public void onCaptureSourcesCallback(final List<MediaSource> mediaSources) {
                videoSources.addAll(mediaSources);
                if (mediaSources.isEmpty()) {
                    Log.e(TAG, "no video sources found");
                }
                latch.countDown();
            }
        });

        try {
            latch.await(SOURCE_CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new CameraSourceImpl(videoSources);
    }
}
