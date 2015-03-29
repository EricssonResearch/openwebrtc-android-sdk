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
import android.util.Log;

import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.Owr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RtcSessionTest extends OwrTestCase {
    private static final String TAG = "RtcSessionTest";

    private MediaSource mVideoSource;
    private MediaSource mAudioSource;

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
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);

        out.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                Log.e(TAG, "LOCAL CANDIDATE out -> in: " + RtcCandidates.toJsep(candidate));
                in.addRemoteCandidate(candidate);
            }
        });
        in.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                Log.e(TAG, "LOCAL CANDIDATE in -> out: " + RtcCandidates.toJsep(candidate));
                out.addRemoteCandidate(candidate);
            }
        });

        final StreamSetMock streamSetMockOut = new StreamSetMock("initiator", Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                audio("audio1", true, true)
        ));

        final StreamSetMock streamSetMockIn = new StreamSetMock("peer", Arrays.asList(
                video("video1", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true)
        ));

        TestUtils.synchronous().timeout(5).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                Owr.getCaptureSources(EnumSet.of(MediaType.VIDEO, MediaType.AUDIO), new CaptureSourcesCallback() {
                    @Override
                    public void onCaptureSourcesCallback(final List<MediaSource> list) {
                        Log.d(TAG, "got capture sources: " + list.size());
                        for (MediaSource source : list) {
                            Log.d(TAG, "iterate source " + source.getName());
                            if (source.getMediaType().contains(MediaType.VIDEO) && mVideoSource == null) {
                                Log.d(TAG, "got video source: " + source + " " + source.getName());
                                mVideoSource = source;
                            } else if (source.getMediaType().contains(MediaType.AUDIO) && mAudioSource == null) {
                                Log.d(TAG, "got audio source: " + source);
                                mAudioSource = source;
                            }
                        }
                        latch.countDown();
                    }
                });
            }
        });

        streamSetMockOut.getStream("audio1").setMediaSource(mAudioSource);
        streamSetMockIn.getStream("audio1").setMediaSource(mAudioSource);
        streamSetMockOut.getStream("video1").setMediaSource(mVideoSource);
        streamSetMockIn.getStream("video1").setMediaSource(mVideoSource);

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setup(streamSetMockOut, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        assertSame(Looper.getMainLooper(), Looper.myLooper());
                        Log.w(TAG, "OFFER: " + SessionDescriptions.toJsep(localDescription));
                        in.setRemoteDescription(localDescription);
                        in.setup(streamSetMockIn, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                assertSame(Looper.getMainLooper(), Looper.myLooper());
                                out.setRemoteDescription(localDescription);
                                Log.w(TAG, "ANSWER: " + SessionDescriptions.toJsep(localDescription));
                                latch.countDown();
                            }
                        });
                    }
                });
                Log.d(TAG, "waiting for call setup");
            }
        });

        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockOut.getStream("video1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockOut.getStream("audio1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockIn.getStream("video1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockIn.getStream("audio1").getStreamMode());
        assertEquals(StreamMode.INACTIVE, streamSetMockOut.getStream("video2").getStreamMode());
        assertEquals(StreamMode.INACTIVE, streamSetMockIn.getStream("audio2").getStreamMode());

        TestUtils.synchronous().timeout(15).latchCount(4).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                streamSetMockOut.getStream("audio1").waitForRemoteSource(latch);
                streamSetMockIn.getStream("audio1").waitForRemoteSource(latch);
                streamSetMockOut.getStream("video1").waitForRemoteSource(latch);
                streamSetMockIn.getStream("video1").waitForRemoteSource(latch);
                Log.d(TAG, "waiting for remote sources");
            }
        });
        Log.d(TAG, "got all remote sources");
    }

    public void testStreamsetCombinations() {
        StreamSetMock streamSetMockOut = new StreamSetMock("initiator", Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                video("video3", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true)
        ));

        final StreamSetMock streamSetMockIn = new StreamSetMock("peer", Arrays.asList(
                video("video1", true, true),
                video("video2", true, true),
                audio("audio1", true, true),
                audio("audio2", true, true),
                audio("audio3", true, true)
        ));

        runStreamSetTest(streamSetMockIn, streamSetMockOut);
    }

    private void runStreamSetTest(final StreamSetMock inbound, final StreamSetMock outbound) {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);

        TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setup(outbound, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        in.setRemoteDescription(localDescription);
                        in.setup(inbound, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                out.setRemoteDescription(localDescription);
                                latch.countDown();
                            }
                        });
                    }
                });
            }
        });
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
        private final String mLabel;

        public StreamSetMock(final String label, List<StreamConfig> configs) {
            mLabel = label;
            mStreams = new ArrayList<>(configs.size());
            for (StreamConfig config : configs) {
                mStreams.add(new MediaStreamMock(config));
            }
        }

        public MediaStreamMock getStream(String id) {
            for (Stream stream : mStreams) {
                MediaStreamMock mock = (MediaStreamMock) stream;
                if (id.equals(mock.getId())) {
                    return mock;
                }
            }
            return null;
        }

        @Override
        protected List<Stream> getStreams() {
            return mStreams;
        }

        private class MediaStreamMock extends MediaStream {
            private MediaSource mRemoteSource;
            private MediaSourceDelegate mMediaSourceDelegate;
            private StreamMode mStreamMode = null;
            private MediaSource mLocalSource;

            private final StreamConfig mConfig;
            private CountDownLatch mCountDownLatch = null;

            public MediaStreamMock(final StreamConfig config) {
                mConfig = config;
            }

            public boolean haveRemoteSource() {
                return mRemoteSource != null;
            }

            public boolean haveMediaSourceDelegate() {
                return mMediaSourceDelegate != null;
            }

            public MediaSourceDelegate getMediaSourceDelegate() {
                return mMediaSourceDelegate;
            }

            public MediaSource getRemoteSource() {
                return mRemoteSource;
            }

            public StreamMode getStreamMode() {
                return mStreamMode;
            }

            public synchronized void waitForRemoteSource(final CountDownLatch remoteSourceLatch) {
                if (haveRemoteSource()) {
                    Log.d(TAG, "[" + mLabel + "] already had remote source for " + getId());
                    remoteSourceLatch.countDown();
                    return;
                }
                Log.d(TAG, "[" + mLabel + "] waiting for remote source for " + getId());
                mCountDownLatch = remoteSourceLatch;
            }

            @Override
            protected String getId() {
                return mConfig.id;
            }

            @Override
            protected MediaType getMediaType() {
                return mConfig.mediaType;
            }

            @Override
            protected boolean wantSend() {
                return mConfig.wantSend;
            }

            @Override
            protected boolean wantReceive() {
                return mConfig.wantReceive;
            }

            @Override
            protected synchronized void onRemoteMediaSource(final MediaSource mediaSource) {
                Log.v(TAG, "[" + mLabel + "] got remote source for " + getId());
                mRemoteSource = mediaSource;
                if (mCountDownLatch != null) {
                    mCountDownLatch.countDown();
                }
            }

            @Override
            protected void setMediaSourceDelegate(final MediaSourceDelegate mediaSourceDelegate) {
                mMediaSourceDelegate = mediaSourceDelegate;
                if (mLocalSource != null) {
                    Log.v(TAG, "[" + mLabel + "] local source set for " + getId() + " : " + mLocalSource);
                    mediaSourceDelegate.setMediaSource(mLocalSource);
                } else {
                    Log.v(TAG, "[" + mLabel + "] local source not set for " + getId() + " : " + mediaSourceDelegate);
                }
            }

            @Override
            public void setStreamMode(final StreamMode mode) {
                mStreamMode = mode;
            }

            public void setMediaSource(final MediaSource mediaSource) {
                Log.v(TAG, "[" + mLabel + "] local source stored for " + getId() + " : " + mediaSource);
                mLocalSource = mediaSource;
            }
        }
    }
}
