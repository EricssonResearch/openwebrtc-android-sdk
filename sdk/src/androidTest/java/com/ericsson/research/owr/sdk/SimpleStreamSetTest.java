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

import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Owr;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SimpleStreamSetTest extends OwrTestCase {
    private static final String TAG = "SimpleStreamSetTest";

    public void testSimpleCall() {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);

        final SimpleStreamSet simpleStreamSetOut = SimpleStreamSet.defaultConfig(true, true);
        final SimpleStreamSet simpleStreamSetIn = SimpleStreamSet.defaultConfig(true, true);

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
        });
    }

    public void testViews() {
        SimpleStreamSet withVideo = SimpleStreamSet.defaultConfig(false, true);
        runViewTest(withVideo);
        SimpleStreamSet withoutVideo = SimpleStreamSet.defaultConfig(false, false);
        runViewTest(withoutVideo);

        TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                Owr.getCaptureSources(EnumSet.of(MediaType.VIDEO), new CaptureSourcesCallback() {
                    @Override
                    public void onCaptureSourcesCallback(final List<MediaSource> list) {
                        latch.countDown();
                    }
                });
            }
        });

        // StreamSets should now have sources
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
        simpleStreamSet.setSelfView(surfaceView);
        simpleStreamSet.setSelfView(surfaceView);
        simpleStreamSet.setRemoteView(surfaceView);
        simpleStreamSet.setRemoteView(surfaceView);
        simpleStreamSet.stopSelfView();
        simpleStreamSet.setSelfView(textureView);
        simpleStreamSet.setRemoteView(textureView);
        simpleStreamSet.stopSelfView();
        simpleStreamSet.stopSelfView();
    }
}
