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

import android.util.Log;

import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Owr;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MicrophoneSourceImpl extends MicrophoneSource {
    private static final String TAG = "MicrophoneSourceImpl";

    private static final long SOURCE_CALLBACK_TIMEOUT_MS = 1000;

    private final MediaSourceListenerSet mListeners = new MediaSourceListenerSet();

    private final MediaSource mAudioSource;

    MicrophoneSourceImpl(MediaSource audioSource) {
        mAudioSource = audioSource;
        mListeners.notifyListeners(audioSource);
    }

    public static MicrophoneSourceImpl create() {
        final MediaSource[] audioSource = new MediaSource[1];

        final CountDownLatch latch = new CountDownLatch(1);
        Owr.getCaptureSources(EnumSet.of(MediaType.AUDIO), new CaptureSourcesCallback() {
            @Override
            public void onCaptureSourcesCallback(final List<MediaSource> mediaSources) {
                if (mediaSources.isEmpty()) {
                    Log.e(TAG, "no audio source found");
                    latch.countDown();
                    return;
                } else if (mediaSources.size() > 1) {
                    Log.e(TAG, "multiple audio sources found, using the first one");
                }
                audioSource[0] = mediaSources.get(0);
                latch.countDown();
            }
        });

        try {
            latch.await(SOURCE_CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new MicrophoneSourceImpl(audioSource[0]);
    }

    @Override
    public String getName() {
        return mAudioSource == null ? "(null)" : mAudioSource.getName();
    }

    @Override
    public void addMediaSourceListener(final MediaSourceListener delegate) {
        mListeners.addListener(delegate);
    }
}
