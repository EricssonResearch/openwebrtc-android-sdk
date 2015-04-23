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

import com.ericsson.research.owr.MediaSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class MediaSourceListenerSet {
    private static final String TAG = "MediaSourceListenerSet";

    private final List<WeakReference<MediaSourceListener>> mListeners = new ArrayList<>();
    private MediaSource mPreviousMediaSource = null;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public synchronized void addListener(final MediaSourceListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener may not be null");
        }
        removeListener(listener);
        mListeners.add(new WeakReference<>(listener));
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.setMediaSource(mPreviousMediaSource);
            }
        });
    }

    private void removeListener(MediaSourceListener removedListener) {
        List<WeakReference<MediaSourceListener>> removed = new ArrayList<>();
        for (WeakReference<MediaSourceListener> ref : mListeners) {
            MediaSourceListener listener = ref.get();
            if (listener == null || listener == removedListener) {
                removed.add(ref);
            }
        }
        mListeners.removeAll(removed);
    }

    public synchronized void notifyListeners(final MediaSource mediaSource) {
        mPreviousMediaSource = mediaSource;
        List<WeakReference<MediaSourceListener>> removed = new ArrayList<>();
        for (WeakReference<MediaSourceListener> ref : mListeners) {
            final MediaSourceListener listener = ref.get();
            if (listener != null) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.setMediaSource(mediaSource);
                    }
                });
            } else {
                removed.add(ref);
            }
        }
        mListeners.removeAll(removed);
    }
}
