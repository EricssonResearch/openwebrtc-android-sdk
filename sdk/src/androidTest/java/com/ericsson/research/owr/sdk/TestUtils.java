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
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestUtils {
    public static class SynchronousCallBuilder {
        private int mTimeout;
        private int mLatchCount;
        private Handler mAsyncHandler;

        private SynchronousCallBuilder(Handler asyncHandler) {
            mTimeout = 5;
            mLatchCount = 1;
            if (asyncHandler != null) {
                mAsyncHandler = asyncHandler;
            } else {
                HandlerThread thread = new HandlerThread("TestUtils-async-thread");
                thread.start();
                mAsyncHandler = new Handler(thread.getLooper());
            }
        }

        public SynchronousCallBuilder timeout(int seconds) {
            mTimeout = seconds;
            return this;
        }

        public SynchronousCallBuilder latchCount(int count) {
            mLatchCount = count;
            return this;
        }

        public SynchronousCallBuilder run(final SynchronousBlock block) {
            final CountDownLatch latch = new CountDownLatch(mLatchCount);
            block.run(latch);
            waitForLatch(latch, 0);
            return new SynchronousCallBuilder(mAsyncHandler);
        }

        public SynchronousCallBuilder run(final Runnable runnable) {
            final CountDownLatch latch = new CountDownLatch(mLatchCount);
            mAsyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    latch.countDown();
                }
            });
            waitForLatch(latch, 0);
            return new SynchronousCallBuilder(mAsyncHandler);
        }

        public SynchronousCallBuilder delay(int delay, final Runnable runnable) {
            if (delay <= 0) {
                throw new IllegalArgumentException("delay should be >= 0");
            }
            final CountDownLatch latch = new CountDownLatch(1);
            mAsyncHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    latch.countDown();
                }
            }, delay);
            waitForLatch(latch, delay);
            return new SynchronousCallBuilder(mAsyncHandler);
        }

        public SynchronousCallBuilder delay(int delay, final SynchronousBlock block) {
            if (delay <= 0) {
                throw new IllegalArgumentException("delay should be >= 0");
            }
            final CountDownLatch latch = new CountDownLatch(mLatchCount);
            mAsyncHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    block.run(latch);
                }
            }, delay);
            waitForLatch(latch, delay);
            return new SynchronousCallBuilder(mAsyncHandler);
        }

        private void waitForLatch(CountDownLatch latch, int extraMilliseconds) {
            try {
                if (!latch.await(extraMilliseconds + mTimeout * 1000, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException("synchronous block timed out");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static SynchronousCallBuilder synchronous() {
        return new SynchronousCallBuilder(null);
    }

    public interface SynchronousBlock {
        void run(CountDownLatch latch);
    }

    public static void waitForNUpdates(final TextureView textureView, int count) {
        TextureView.SurfaceTextureListener previousListener = textureView.getSurfaceTextureListener();
        final TextureViewAsserter textureViewAsserter = new TextureViewAsserter(previousListener);
        textureView.setSurfaceTextureListener(textureViewAsserter);
        TestUtils.synchronous().latchCount(count).timeout(15).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                textureViewAsserter.waitForUpdates(latch);
            }
        });
        textureView.setSurfaceTextureListener(previousListener);
    }

    private static class TextureViewAsserter implements TextureView.SurfaceTextureListener {
        private final TextureView.SurfaceTextureListener mListener;
        private CountDownLatch mLatch;

        public TextureViewAsserter(final TextureView.SurfaceTextureListener surfaceTextureListener) {
            mListener = surfaceTextureListener;
        }

        public void waitForUpdates(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            mListener.onSurfaceTextureAvailable(surface, width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
            mListener.onSurfaceTextureSizeChanged(surface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            return mListener.onSurfaceTextureDestroyed(surface);
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
            if (mLatch != null) {
                mLatch.countDown();
            }
            mListener.onSurfaceTextureUpdated(surface);
        }
    }
}
