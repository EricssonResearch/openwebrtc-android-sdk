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

import com.ericsson.research.owr.MediaSource;

import java.util.concurrent.CountDownLatch;

public class MediaSourceTests extends OwrTestCase {
    private static final String TAG = "MediaSourceTests";

    private int mListenerCount = 0;

    public class MockListener implements MediaSourceListener {
        private final CountDownLatch mLatch;

        public MockListener() {
            mLatch = null;
        }

        public MockListener(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void setMediaSource(final MediaSource mediaSource) {
            mListenerCount += 1;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
    }

    public void testMediaSourceListenerSet() {
        final MediaSourceListenerSet set = new MediaSourceListenerSet();
        set.addListener(new MockListener());
        mListenerCount = 0;
        set.notifyListeners(null);
        assertEquals(1, mListenerCount);

        long freeMemBeforeGc = Runtime.getRuntime().freeMemory();
        System.gc();
        if (Runtime.getRuntime().freeMemory() < freeMemBeforeGc) {
            set.notifyListeners(null);
            assertEquals(1, mListenerCount);
            TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
                @Override
                public void run(final CountDownLatch latch) {
                    set.addListener(new MockListener(latch));
                }
            });
            assertEquals(2, mListenerCount); // should be signaled when it's added
        } else {
            Log.w(TAG, "Skipping GC test, no memory was free'd");
        }

        try {
            set.addListener(null);
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException ignored) {}
    }
}
