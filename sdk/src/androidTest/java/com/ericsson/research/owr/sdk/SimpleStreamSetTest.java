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

import android.view.SurfaceView;
import android.view.TextureView;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class SimpleStreamSetTest extends OwrTestCase {
    private static final String TAG = "SimpleStreamSetTest";

    public void testSimpleCall() {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);

        final SimpleStreamSet simpleStreamSetOut = SimpleStreamSet.defaultConfig(true, true);
        final SimpleStreamSet simpleStreamSetIn = SimpleStreamSet.defaultConfig(true, true);

        assertEquals(2, simpleStreamSetOut.getVideoSourceCount());
        assertEquals(2, simpleStreamSetIn.getVideoSourceCount());

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setOnLocalDescriptionListener(new RtcSession.OnLocalDescriptionListener() {
                    @Override
                    public void onLocalDescription(final SessionDescription localDescription) {
                        try {
                            in.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in.start(simpleStreamSetIn);
                    }
                });
                in.setOnLocalDescriptionListener(new RtcSession.OnLocalDescriptionListener() {
                    @Override
                    public void onLocalDescription(final SessionDescription localDescription) {
                        try {
                            out.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        latch.countDown();
                    }
                });
                out.start(simpleStreamSetOut);
            }
        }).delay(5000, new Runnable() {
            @Override
            public void run() {
                assertEquals(0, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
                assertSame(SimpleStreamSet.VideoSourceState.READY, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.READY, simpleStreamSetOut.getVideoSourceState());
                simpleStreamSetIn.setVideoSourceIndex(0); // already using 0, so should have no effect
                simpleStreamSetOut.setVideoSourceIndex(0);
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
                assertSame(SimpleStreamSet.VideoSourceState.READY, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.READY, simpleStreamSetOut.getVideoSourceState());
                assertEquals(0, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
                simpleStreamSetIn.setVideoSourceIndex(1);
                simpleStreamSetOut.setVideoSourceIndex(1);
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(1, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(300, new Runnable() {
            @Override
            public void run() { // we're closing the old source
                simpleStreamSetIn.setVideoSourceIndex(0);
                simpleStreamSetOut.setVideoSourceIndex(0);
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(0, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(300, new Runnable() {
            @Override
            public void run() { // we're still closing the old source
                simpleStreamSetIn.setVideoSourceIndex(1);
                simpleStreamSetOut.setVideoSourceIndex(1);
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(1, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(900, new Runnable() {
            @Override
            public void run() { // We should now have switched source
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(1, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
                simpleStreamSetIn.setVideoSourceIndex(0);
                simpleStreamSetOut.setVideoSourceIndex(0);
                assertEquals(0, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(5000, new Runnable() {
            @Override
            public void run() { // in the opening state
                simpleStreamSetIn.setVideoSourceIndex(1);
                simpleStreamSetOut.setVideoSourceIndex(1);
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(1, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(1000, new Runnable() {
            @Override
            public void run() { // switch back to 0
                simpleStreamSetIn.setVideoSourceIndex(0);
                simpleStreamSetOut.setVideoSourceIndex(0);
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(0, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(2000, new Runnable() {
            @Override
            public void run() { // open should now be complete, so it should now be switching
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.CLOSING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(0, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(0, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
                simpleStreamSetIn.setVideoSourceIndex(1);
                simpleStreamSetOut.setVideoSourceIndex(1);
                assertEquals(1, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
            }
        }).delay(2000, new Runnable() {
            @Override
            public void run() {
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.OPENING, simpleStreamSetOut.getVideoSourceState());
                assertEquals(1, simpleStreamSetIn.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getVideoSourceIndex());
                assertEquals(1, simpleStreamSetIn.getActiveVideoSourceIndex());
                assertEquals(1, simpleStreamSetOut.getActiveVideoSourceIndex());
                out.stop();
                in.stop();
                simpleStreamSetOut.stopSelfView();
                simpleStreamSetIn.stopSelfView();
            }
        }).delay(9000, new Runnable() {
            @Override
            public void run() {
                assertSame(SimpleStreamSet.VideoSourceState.READY, simpleStreamSetIn.getVideoSourceState());
                assertSame(SimpleStreamSet.VideoSourceState.READY, simpleStreamSetOut.getVideoSourceState());
                simpleStreamSetIn.setVideoSourceIndex(1);
                simpleStreamSetOut.setVideoSourceIndex(1);
                TestUtils.sleep(1000);
            }
        });
    }

    public void testViews() {
        SimpleStreamSet withVideo = SimpleStreamSet.defaultConfig(false, true);
        runViewTest(withVideo);
        SimpleStreamSet withoutVideo = SimpleStreamSet.defaultConfig(false, false);
        runViewTest(withoutVideo);

        runViewTest(withVideo);
        runViewTest(withoutVideo);
    }

    private void runViewTest(SimpleStreamSet simpleStreamSet) {
        TextureView textureView = new TextureView(getContext(), null);
        SurfaceView surfaceView = new SurfaceView(getContext(), null);

        simpleStreamSet.stopSelfView();
        simpleStreamSet.setSelfView(textureView);
        simpleStreamSet.setRemoteView(textureView);
        simpleStreamSet.setSelfView(textureView);
        simpleStreamSet.setRemoteView(textureView);
        simpleStreamSet.stopSelfView();
        simpleStreamSet.stopRemoteView();
        simpleStreamSet.setSelfView(surfaceView);
        simpleStreamSet.setSelfView(surfaceView);
        simpleStreamSet.setRemoteView(surfaceView);
        simpleStreamSet.setRemoteView(surfaceView);
        simpleStreamSet.stopSelfView();
        simpleStreamSet.setSelfView(textureView);
        simpleStreamSet.setRemoteView(textureView);
        simpleStreamSet.stopSelfView();
        simpleStreamSet.stopRemoteView();
        simpleStreamSet.stopSelfView();
    }

    public void testSwitchVideoSource() {
        SurfaceView surfaceView = new SurfaceView(getContext(), null);
        SimpleStreamSet setSourceAfterView = SimpleStreamSet.defaultConfig(false, true);
        runViewTest(setSourceAfterView);

        assertEquals(2, setSourceAfterView.getVideoSourceCount());
        setSourceAfterView.setSelfView(surfaceView);
        setSourceAfterView.setVideoSourceIndex(1);
        setSourceAfterView.stopSelfView();

        SimpleStreamSet setSourceBeforeView = SimpleStreamSet.defaultConfig(false, true);
        runViewTest(setSourceBeforeView);

        assertEquals(2, setSourceBeforeView.getVideoSourceCount());
        setSourceBeforeView.setVideoSourceIndex(1);
        setSourceBeforeView.setSelfView(surfaceView);
    }
}
