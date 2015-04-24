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

import android.test.MoreAsserts;
import android.util.Log;
import android.view.TextureView;

import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.SourceType;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MediaSourceTests extends OwrActivityTestCase {
    private static final String TAG = "MediaSourceTests";

    private int mListenerCallCount = 0;

    public class MockListener implements MediaSourceListener {
        private final CountDownLatch mLatch;

        public MockListener(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void setMediaSource(final MediaSource mediaSource) {
            mListenerCallCount += 1;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
    }

    public void testMediaSourceListenerSet() {
        final MediaSourceListenerSet set = new MediaSourceListenerSet();
        TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                set.addListener(new MockListener(latch));
            }
        });
        assertEquals(1, mListenerCallCount);
        set.notifyListeners(null);

        long freeMemBeforeGc = Runtime.getRuntime().freeMemory();
        System.gc();
        TestUtils.sleep(500);
        if (Runtime.getRuntime().freeMemory() < freeMemBeforeGc) {
            set.notifyListeners(null); // This should not call the listener, as it was GC'd
            assertEquals(1, mListenerCallCount);
            TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
                @Override
                public void run(final CountDownLatch latch) {
                    set.addListener(new MockListener(latch));
                }
            });
            // The previous assertion would fail here, as we didn't handle that it was asynchronous
            assertEquals(2, mListenerCallCount); // should be signaled when it's added
        } else {
            Log.w(TAG, "Skipping GC test, no memory was free'd");
        }

        try {
            set.addListener(null);
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException ignored) {}
    }

    public void testMicrophoneSource() {
        final MicrophoneSource source = MicrophoneSource.getInstance();
        assertEquals("Default audio input", source.getName());
        final MediaSource[] audioSource = new MediaSource[1];
        TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                source.addMediaSourceListener(new MediaSourceListener() {
                    @Override
                    public void setMediaSource(final MediaSource mediaSource) {
                        assertNotNull(mediaSource);
                        audioSource[0] = mediaSource;
                        MoreAsserts.assertContentsInOrder(mediaSource.getMediaType(), MediaType.AUDIO);
                        assertEquals(source.getName(), mediaSource.getName());
                        assertSame(SourceType.CAPTURE, mediaSource.getType());
                        latch.countDown();
                    }
                });
            }
        }).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                source.addMediaSourceListener(new MediaSourceListener() {
                    @Override
                    public void setMediaSource(final MediaSource mediaSource) {
                        assertNotNull(mediaSource);
                        assertSame(audioSource[0], mediaSource);
                        latch.countDown();
                    }
                });
            }
        });
    }

    public void testSelfView() {
        CameraSource cameraSource = CameraSource.getInstance();
        VideoView videoView = cameraSource.createVideoView();

        assertEquals(2, cameraSource.getCount()); // Test needs to be run on a device with two cameras
        assertEquals("Front facing Camera", cameraSource.getName(0));
        assertEquals("Back facing Camera", cameraSource.getName(1));
        List<String> emptyPipelineDump = cameraSource.dumpPipelineGraphs();
        assertEquals(2, emptyPipelineDump.size());
        assertNotNull(emptyPipelineDump.get(0));
        assertNotNull(emptyPipelineDump.get(1));

        TextureView textureView = getActivity().getTextureView();

        assertEquals(0, videoView.getRotation());
        videoView.setRotation(0);
        videoView.setView(textureView);
        TestUtils.waitForNUpdates(textureView, 5);
        videoView.setRotation(1);
        videoView.setView(textureView);
        assertEquals(1, videoView.getRotation());
        TestUtils.waitForNUpdates(textureView, 5);
        try {
            videoView.setView(null);
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException ignored) {}
        videoView.stop();
        videoView.stop();
        videoView.stop();
        videoView.setView(textureView);
        TestUtils.waitForNUpdates(textureView, 5);
        videoView.setRotation(1);
        assertEquals(1, videoView.getRotation());
        TestUtils.sleep(100);
        videoView.setRotation(2);
        assertEquals(2, videoView.getRotation());
        TestUtils.sleep(100);
        videoView.setRotation(3);
        assertEquals(3, videoView.getRotation());
        TestUtils.sleep(100);
        TestUtils.waitForNUpdates(textureView, 5);
        try {
            videoView.setRotation(4);
            throw new RuntimeException("should not be reached");
        } catch (IllegalArgumentException ignored) {}
        try {
            videoView.setRotation(-1);
            throw new RuntimeException("should not be reached");
        } catch (IllegalArgumentException ignored) {}
        try {
            videoView.setRotation(90);
            throw new RuntimeException("should not be reached");
        } catch (IllegalArgumentException ignored) {}
        videoView.stop();
        TestUtils.sleep(1000);
    }

    public void testSelfViewSwitching() {
        final CameraSourceImpl cameraSource = (CameraSourceImpl) CameraSource.getInstance();
        final VideoView videoView = cameraSource.createVideoView();
        final TextureView textureView = getActivity().getTextureView();

        videoView.setView(textureView);
        TestUtils.waitForNUpdates(textureView, 5);

        TestUtils.synchronous().delay(5000, new Runnable() {
            @Override
            public void run() {
                assertEquals(0, cameraSource.getSelectedSource());
                assertEquals(0, cameraSource.getActiveSource());
                assertSame(CameraSourceImpl.VideoSourceState.READY, cameraSource.getVideoSourceState());
                cameraSource.selectSource(0);
                assertEquals(0, cameraSource.getActiveSource());
                assertSame(CameraSourceImpl.VideoSourceState.READY, cameraSource.getVideoSourceState());
                assertEquals(0, cameraSource.getSelectedSource());
                assertEquals(0, cameraSource.getActiveSource());
                cameraSource.selectSource(1);
                assertEquals(0, cameraSource.getActiveSource());
                assertSame(CameraSourceImpl.VideoSourceState.CLOSING, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(0, cameraSource.getActiveSource());
            }
        }).delay(300, new Runnable() {
            @Override
            public void run() { // we're closing the old source
                cameraSource.selectSource(0);
                assertSame(CameraSourceImpl.VideoSourceState.CLOSING, cameraSource.getVideoSourceState());
                assertEquals(0, cameraSource.getSelectedSource());
                assertEquals(0, cameraSource.getActiveSource());
            }
        }).delay(300, new Runnable() {
            @Override
            public void run() { // we're still closing the old source
                cameraSource.selectSource(1);
                assertSame(CameraSourceImpl.VideoSourceState.CLOSING, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(0, cameraSource.getActiveSource());
            }
        }).delay(900, new Runnable() {
            @Override
            public void run() { // We should now have switched source
                assertSame(CameraSourceImpl.VideoSourceState.OPENING, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
                cameraSource.selectSource(0);
                assertEquals(0, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
            }
        }).delay(5000, new Runnable() {
            @Override
            public void run() { // in the opening state
                cameraSource.selectSource(1);
                assertSame(CameraSourceImpl.VideoSourceState.OPENING, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
            }
        }).delay(2000, new Runnable() {
            @Override
            public void run() { // switch back to 0
                assertSame(CameraSourceImpl.VideoSourceState.OPENING, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
            }
        }).delay(2000, new Runnable() {
            @Override
            public void run() { // open should now be complete
                assertSame(CameraSourceImpl.VideoSourceState.READY, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
            }
        }).delay(2000, new Runnable() {
            @Override
            public void run() {
                assertSame(CameraSourceImpl.VideoSourceState.READY, cameraSource.getVideoSourceState());
                assertEquals(1, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
                cameraSource.selectSource(0); // Switch back to 0 to not break future tests
                videoView.stop();
                assertSame(CameraSourceImpl.VideoSourceState.CLOSING, cameraSource.getVideoSourceState());
                assertEquals(0, cameraSource.getSelectedSource());
                assertEquals(1, cameraSource.getActiveSource());
            }
        }).delay(10000, new Runnable() {
            @Override
            public void run() {
                assertSame(CameraSourceImpl.VideoSourceState.READY, cameraSource.getVideoSourceState());
                assertEquals(0, cameraSource.getSelectedSource());
                assertEquals(0, cameraSource.getActiveSource());
            }
        });
    }
}
