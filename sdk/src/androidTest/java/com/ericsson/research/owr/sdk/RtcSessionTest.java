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

import android.os.Looper;
import android.test.AndroidTestCase;

import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RtcSessionTest extends AndroidTestCase {
    private static final String TAG = "RtcSessionTest";

    public void testStuff() {
        RtcConfig config = RtcConfigs.defaultConfig("stun.example.com");
        final RtcSession session = RtcSessions.create(config);
        session.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
            }
        });
        session.end();
    }

    public void testCall() {
        final CountDownLatch lock = new CountDownLatch(1);

        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);

        out.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                in.addRemoteCandidate(candidate);
            }
        });
        in.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                out.addRemoteCandidate(candidate);
            }
        });

        StreamSetMock streamSetMockOut = new StreamSetMock(Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                video("video3", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true)
        ));

        final StreamSetMock streamSetMockIn = new StreamSetMock(Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true),
                audio("audio3", true, true)
        ));

        out.setup(streamSetMockOut, new RtcSession.SetupCompleteCallback() {
            @Override
            public void onSetupComplete(final SessionDescription localDescription) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                in.setRemoteDescription(localDescription);
                in.setup(streamSetMockIn, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        assertSame(Looper.getMainLooper(), Looper.myLooper());
                        out.setRemoteDescription(localDescription);
                        lock.countDown();
                    }
                });
            }
        });

        try {
            lock.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("session setup timed out", e);
        }

/*
//        Java 8

        out.setOnLocalCandidateListener(in::addRemoteCandidate);
        in.setOnLocalCandidateListener(out::addRemoteCandidate);

        out.setup(simpleStreamSetOut, remoteDesc -> {
            in.setRemoteDescription(remoteDesc);
            in.setup(simpleStreamSetIn, out::setRemoteDescription);
        });
*/
    }

    public void testStreamsetCombinations() {
        StreamSetMock streamSetMockOut = new StreamSetMock(Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                video("video3", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true)
        ));

        final StreamSetMock streamSetMockIn = new StreamSetMock(Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true),
                audio("audio3", true, true)
        ));

        runStreamSetTest(streamSetMockIn, streamSetMockOut);
    }

    private void runStreamSetTest(final StreamSetMock inbound, StreamSetMock outbound) {
        final CountDownLatch lock = new CountDownLatch(1);

        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);


        out.setup(outbound, new RtcSession.SetupCompleteCallback() {
            @Override
            public void onSetupComplete(final SessionDescription localDescription) {
                in.setRemoteDescription(localDescription);
                in.setup(inbound, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        out.setRemoteDescription(localDescription);
                        lock.countDown();
                    }
                });
            }
        });

        try {
            lock.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("session setup timed out", e);
        }
    }

    private static StreamConfig video(String id, boolean wantSend, boolean wantReceive) {
        return new StreamConfig(id, wantSend, wantReceive, MediaType.VIDEO);
    }

    private static StreamConfig audio(String id, boolean wantSend, boolean wantReceive) {
        return new StreamConfig(id, wantSend, wantReceive, MediaType.AUDIO);
    }

    private static class StreamConfig {
        private String id;
        private boolean wantSend;
        private boolean wantReceive;
        private MediaType mediaType;

        private StreamConfig(String id, boolean wantSend, boolean wantReceive, MediaType mediaType) {
            this.id = id;
            this.wantSend = wantSend;
            this.wantReceive = wantReceive;
            this.mediaType = mediaType;
        }
    }

    private class StreamSetMock extends StreamSet {
        private final ArrayList<Stream> mStreams;

        public StreamSetMock(List<StreamConfig> configs) {
            mStreams = new ArrayList<>(configs.size());
            for (StreamConfig config : configs) {
                mStreams.add(new MediaStreamMock(config));
            }
        }

        @Override
        List<Stream> getStreams() {
            return mStreams;
        }

        private class MediaStreamMock extends MediaStream {
            private boolean mGotRemoteSource;
            private boolean mGotMediaSourceDelegate;
            private StreamMode mStreamMode;

            private final StreamConfig mConfig;

            public MediaStreamMock(final StreamConfig config) {
                mConfig = config;
            }

            @Override
            String getId() {
                return mConfig.id;
            }

            @Override
            MediaType getMediaType() {
                return mConfig.mediaType;
            }

            @Override
            boolean wantSend() {
                return mConfig.wantSend;
            }

            @Override
            boolean wantReceive() {
                return mConfig.wantReceive;
            }

            @Override
            void onRemoteMediaSource(final MediaSource mediaSource) {
                mGotRemoteSource = true;
            }

            @Override
            void setMediaSourceDelegate(final MediaSourceDelegate mediaSourceDelegate) {
                mGotMediaSourceDelegate = true;
            }

            @Override
            public void setStreamMode(final StreamMode mode) {
                mStreamMode = mode;
            }
        }
    }
}
