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

import com.ericsson.research.owr.AudioRenderer;
import com.ericsson.research.owr.CaptureSourcesCallback;
import com.ericsson.research.owr.DataChannel;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.DataChannelReadyState;
import com.ericsson.research.owr.Owr;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
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
        session.stop();
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

        streamSetMockOut.getMediaStream("audio1").setMediaSource(mAudioSource);
        streamSetMockIn.getMediaStream("audio1").setMediaSource(mAudioSource);
        streamSetMockOut.getMediaStream("video1").setMediaSource(mVideoSource);
        streamSetMockIn.getMediaStream("video1").setMediaSource(mVideoSource);

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setup(streamSetMockOut, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        assertSame(Looper.getMainLooper(), Looper.myLooper());
                        Log.w(TAG, "OFFER: " + SessionDescriptions.toJsep(localDescription));
                        try {
                            in.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in.setup(streamSetMockIn, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                assertSame(Looper.getMainLooper(), Looper.myLooper());
                                try {
                                    out.setRemoteDescription(localDescription);
                                } catch (InvalidDescriptionException e) {
                                    throw new RuntimeException(e);
                                }
                                Log.w(TAG, "ANSWER: " + SessionDescriptions.toJsep(localDescription));
                                latch.countDown();
                            }
                        });
                    }
                });
                Log.d(TAG, "waiting for call setup");
            }
        });

        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockOut.getMediaStream("video1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockOut.getMediaStream("audio1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockIn.getMediaStream("video1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockIn.getMediaStream("audio1").getStreamMode());
        assertEquals(StreamMode.INACTIVE, streamSetMockOut.getMediaStream("video2").getStreamMode());
        assertEquals(StreamMode.INACTIVE, streamSetMockIn.getMediaStream("audio2").getStreamMode());

        TestUtils.synchronous().timeout(15).latchCount(4).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                streamSetMockOut.getMediaStream("audio1").waitForRemoteSource(latch);
                streamSetMockIn.getMediaStream("audio1").waitForRemoteSource(latch);
                streamSetMockOut.getMediaStream("video1").waitForRemoteSource(latch);
                streamSetMockIn.getMediaStream("video1").waitForRemoteSource(latch);
                Log.d(TAG, "waiting for remote sources");
            }
        });

        Log.d(TAG, "got all remote sources");

        Log.d(TAG, "testing stop");
        out.stop();
        in.stop();
        try {
            Thread.sleep(200); // wait a bit for stop to complete
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Log.d(TAG, "stopped");
    }

    public void testJsepCall() {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);

        out.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                JSONObject json = RtcCandidates.toJsep(candidate);
                Log.e(TAG, "LOCAL CANDIDATE out -> in: " + json);
                in.addRemoteCandidate(RtcCandidates.fromJsep(json));
            }
        });
        in.setOnLocalCandidateListener(new RtcSession.OnLocalCandidateListener() {
            @Override
            public void onLocalCandidate(final RtcCandidate candidate) {
                assertSame(Looper.getMainLooper(), Looper.myLooper());
                JSONObject json = RtcCandidates.toJsep(candidate);
                Log.e(TAG, "LOCAL CANDIDATE in -> out: " + json);
                out.addRemoteCandidate(RtcCandidates.fromJsep(json));
            }
        });

        final StreamSetMock streamSetMockOut = new StreamSetMock("initiator", Arrays.asList(
                video("video1", true, true),
                audio("audio1", true, true)
        ));

        final StreamSetMock streamSetMockIn = new StreamSetMock("peer", Arrays.asList(
                video("video1", true, true),
                audio("audio1", true, true)
        ));

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setup(streamSetMockOut, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        assertSame(Looper.getMainLooper(), Looper.myLooper());
                        JSONObject jsepOffer = SessionDescriptions.toJsep(localDescription);
                        try {
                            in.setRemoteDescription(SessionDescriptions.fromJsep(jsepOffer));
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in.setup(streamSetMockIn, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                assertSame(Looper.getMainLooper(), Looper.myLooper());
                                JSONObject jsepAnswer = SessionDescriptions.toJsep(localDescription);
                                try {
                                    out.setRemoteDescription(SessionDescriptions.fromJsep(jsepAnswer));
                                } catch (InvalidDescriptionException e) {
                                    throw new RuntimeException(e);
                                }
                                latch.countDown();
                            }
                        });
                    }
                });
                Log.d(TAG, "waiting for call setup");
            }
        });

        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockOut.getMediaStream("video1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockOut.getMediaStream("audio1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockIn.getMediaStream("video1").getStreamMode());
        assertEquals(StreamMode.SEND_RECEIVE, streamSetMockIn.getMediaStream("audio1").getStreamMode());

        out.stop();
        in.stop();

        Log.d(TAG, "stopped");
    }

    public void testStopAndStartWithNew() {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession out = RtcSessions.create(config);
        final RtcSession in = RtcSessions.create(config);
        final RtcSession in2 = RtcSessions.create(config);

        final StreamSetMock streamSetMock = new StreamSetMock("simple", Collections.singletonList(
                audio("audio1", true, true)
        ));

        final SessionDescription[] outOffer = new SessionDescription[1];

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        outOffer[0] = localDescription;
                        try {
                            in.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                in.stop();
                                latch.countDown();
                            }
                        });
                    }
                });
                Log.d(TAG, "waiting for 1/2 call setup");
            }
        });

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                try {
                    in2.setRemoteDescription(outOffer[0]);
                } catch (InvalidDescriptionException e) {
                    throw new RuntimeException(e);
                }
                in2.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        try {
                            out.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in2.stop();
                        latch.countDown();
                    }
                });
                Log.d(TAG, "waiting rest 1/2 of call setup");
            }
        });

        out.stop();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void testImmediateStop() {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession session1 = RtcSessions.create(config);
        final RtcSession session2 = RtcSessions.create(config);

        final StreamSetMock streamSetMock = new StreamSetMock("simple", Collections.singletonList(
                audio("audio1", true, true)
        ));

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                session1.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        throw new RuntimeException("should not be reached");
                    }
                });
                session1.stop();
                session2.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        try {
                            session1.setRemoteDescription(localDescription);
                            throw new RuntimeException("should not be reached");
                        } catch (IllegalStateException e) {
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        session2.stop();
                        latch.countDown();
                    }
                });
                Log.d(TAG, "waiting for call setup");
            }
        });

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void testDatachannel() {
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

        final StreamSetMock streamSetOut = new StreamSetMock("initiator", Collections.singletonList(data()));
        final StreamSetMock streamSetIn = new StreamSetMock("peer", Collections.singletonList(data()));

        TestUtils.synchronous().timeout(30).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                out.setup(streamSetOut, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        Log.w(TAG, "OFFER: " + SessionDescriptions.toJsep(localDescription));
                        try {
                            in.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in.setup(streamSetIn, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                try {
                                    out.setRemoteDescription(localDescription);
                                } catch (InvalidDescriptionException e) {
                                    throw new RuntimeException(e);
                                }
                                Log.w(TAG, "ANSWER: " + SessionDescriptions.toJsep(localDescription));
                                latch.countDown();
                            }
                        });
                    }
                });
                Log.d(TAG, "waiting for call setup");
            }
        });

        TestUtils.synchronous().latchCount(2).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                Log.d(TAG, "waiting for streams to become active");
                streamSetOut.getDataStream().waitUntilActive(latch);
                streamSetIn.getDataStream().waitUntilActive(latch);
            }
        });

        assertSame(StreamMode.SEND_RECEIVE, streamSetOut.getDataStream().getStreamMode());
        assertSame(StreamMode.SEND_RECEIVE, streamSetIn.getDataStream().getStreamMode());

        Log.d(TAG, "streams are active");

/*        final DataChannel channelOut1 = new DataChannel(true, -1, 10, "UTPE", false, (short) 1, "test");
        DataChannel.ReadyStateChangeListener readyStateChangeListener = new DataChannel.ReadyStateChangeListener() {
            @Override
            public void onReadyStateChanged(final DataChannelReadyState dataChannelReadyState) {
                Log.d(TAG, "DATACHANNEL: ready state changed: " + dataChannelReadyState);
            }
        };
        channelOut1.addReadyStateChangeListener(readyStateChangeListener);
        streamSetOut.getDataStream().addDataChannel(channelOut1);

        TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                streamSetIn.getDataStream().waitForDataChannels(latch);
            }
        });

        final DataChannel channelIn1 = streamSetIn.getDataStream().getReceivedDataChannels().get(0);
        channelIn1.addReadyStateChangeListener(readyStateChangeListener);

        runDataChannelMessageTest("requested", channelOut1, channelIn1);

        final DataChannel channelOut2 = new DataChannel(false, 1000, -1, "TEST", true, (short) 3, "test2");
        final DataChannel channelIn2 = new DataChannel(false, 1000, -1, "TEST", true, (short) 3, "test2");
        channelOut2.addReadyStateChangeListener(readyStateChangeListener);
        channelIn2.addReadyStateChangeListener(readyStateChangeListener);
        streamSetOut.getDataStream().addDataChannel(channelOut2);
        streamSetIn.getDataStream().addDataChannel(channelIn2);

        runDataChannelMessageTest("pre-negotiated", channelOut2, channelIn2);

        out.stop();
        in.stop();*/
    }

    private void runDataChannelMessageTest(final String label, final DataChannel left, final DataChannel right) {
        final Handler handler = new Handler(Looper.getMainLooper());
        TestUtils.synchronous().latchCount(4).run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                DataChannel.OnDataListener latchCounter = new DataChannel.OnDataListener() {
                    @Override
                    public void onData(final String string) {
                        Log.d(TAG, "[" + label + "] got string data: " + string);
                        if ("message".equals(string)) {
                            latch.countDown();
                        }
                    }
                };
                DataChannel.OnBinaryDataListener binaryLatchCounter = new DataChannel.OnBinaryDataListener() {
                    @Override
                    public void onBinaryData(final byte[] bytes) {
                        String string = new String(bytes);
                        Log.d(TAG, "[" + label + "] got binary data: " + string);
                        if ("message".equals(string)) {
                            latch.countDown();
                        }
                    }
                };
                left.addOnDataListener(latchCounter);
                left.addOnBinaryDataListener(binaryLatchCounter);
                right.addOnDataListener(latchCounter);
                right.addOnBinaryDataListener(binaryLatchCounter);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "[" + label + "] sending 'message' left -> right");
                        left.send("message");
                        left.sendBinary("message".getBytes());
                        Log.d(TAG, "[" + label + "] sending 'message' right -> left");
                        right.send("message");
                        right.sendBinary("message".getBytes());
                    }
                }, 100);
            }
        });
    }

    public void testInvalidCalls() {
        RtcConfig config = RtcConfigs.defaultConfig(Collections.<RtcConfig.HelperServer>emptyList());
        final RtcSession session = RtcSessions.create(config);
        final StreamSetMock streamSetMock = new StreamSetMock("empty", Collections.<StreamConfig>emptyList());
        try {
            session.setRemoteDescription(null); // invalid argument
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException e) {
        } catch (InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }
        try {
            session.setup(streamSetMock, null); // invalid arguments
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException e) {
        }
        try {
            session.setup(null, new RtcSession.SetupCompleteCallback() { // invalid arguments
                @Override
                public void onSetupComplete(final SessionDescription localDescription) {
                }
            });
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException e) {
        }
        try {
            session.setup(null, null); // invalid arguments
            throw new RuntimeException("should not be reached");
        } catch (NullPointerException e) {
        }

        TestUtils.synchronous().run(new TestUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                session.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                    @Override
                    public void onSetupComplete(final SessionDescription localDescription) {
                        try {
                            // should throw illegal state, since we're already set up
                            session.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                                @Override
                                public void onSetupComplete(final SessionDescription localDescription) {
                                }
                            });
                            throw new RuntimeException("should not be reached");
                        } catch (IllegalStateException e) {
                        }

                        session.stop(); // STOPPING HERE
                        latch.countDown();
                    }
                });
                try {
                    // it should not be possible to set remote description during setup
                    session.setRemoteDescription(new SessionDescriptionImpl(null, null, null));
                    throw new RuntimeException("should not be reached");
                } catch (IllegalStateException e) {
                } catch (InvalidDescriptionException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // should throw illegal state, since we've already started setup
                    session.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                        @Override
                        public void onSetupComplete(final SessionDescription localDescription) {
                        }
                    });
                    throw new RuntimeException("should not be reached");
                } catch (IllegalStateException e) {
                }
            }
        });

        try {
            // should not be possible to set now since it's stopped
            session.setRemoteDescription(new SessionDescriptionImpl(null, null, null));
            throw new RuntimeException("should not be reached");
        } catch (IllegalStateException e) {
        } catch (InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }
        try {
            // should not be possible to set up now since it's stopped
            session.setup(streamSetMock, new RtcSession.SetupCompleteCallback() {
                @Override
                public void onSetupComplete(final SessionDescription localDescription) {
                }
            });
            throw new RuntimeException("should not be reached");
        } catch (IllegalStateException e) {
        }
        // should be fine to call even when stopped
        session.addRemoteCandidate(RtcCandidates.fromSdpAttribute("candidate:1 1 UDP 123 1.1.1.1 1 typ host"));
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
                        try {
                            in.setRemoteDescription(localDescription);
                        } catch (InvalidDescriptionException e) {
                            throw new RuntimeException(e);
                        }
                        in.setup(inbound, new RtcSession.SetupCompleteCallback() {
                            @Override
                            public void onSetupComplete(final SessionDescription localDescription) {
                                try {
                                    out.setRemoteDescription(localDescription);
                                } catch (InvalidDescriptionException e) {
                                    throw new RuntimeException(e);
                                }
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

    private static StreamConfig data() {
        return new StreamConfig();
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

        private StreamConfig() {
            this.mediaType = null;
        }
    }

    private class StreamSetMock extends StreamSet {
        private final ArrayList<Stream> mStreams;
        private final String mLabel;

        public StreamSetMock(final String label, List<StreamConfig> configs) {
            mLabel = label;
            mStreams = new ArrayList<>(configs.size());
            for (StreamConfig config : configs) {
                if (config.mediaType != null) {
                    mStreams.add(new MediaStreamMock(config));
                } else {
                    mStreams.add(new DataStreamMock());
                }
            }
        }

        public MediaStreamMock getMediaStream(String id) {
            for (Stream stream : mStreams) {
                if (stream instanceof MediaStreamMock) {
                    MediaStreamMock mock = (MediaStreamMock) stream;
                    if (id.equals(mock.getId())) {
                        return mock;
                    }
                }
            }
            return null;
        }

        public DataStreamMock getDataStream() {
            for (Stream stream : mStreams) {
                if (stream instanceof DataStreamMock) {
                    return (DataStreamMock) stream;
                }
            }
            return null;
        }

        @Override
        protected List<Stream> getStreams() {
            return mStreams;
        }

        private class DataStreamMock extends DataStream {
            private StreamMode mStreamMode = null;
            private List<DataChannel> mReceivedDataChannels = new LinkedList<>();
            private CountDownLatch mDataChannelLatch = null;
            private CountDownLatch mModeSetLatch = null;
            private DataChannelDelegate mDataChannelDelegate;

            public synchronized void waitForDataChannels(CountDownLatch latch) {
                mDataChannelLatch = latch;
                for (DataChannel ignored : mReceivedDataChannels) {
                    latch.countDown();
                }
            }

            public StreamMode getStreamMode() {
                return mStreamMode;
            }

            public List<DataChannel> getReceivedDataChannels() {
                return mReceivedDataChannels;
            }

            public void addDataChannel(DataChannel dataChannel) {
                mDataChannelDelegate.addDataChannel(dataChannel);
            }

            @Override
            protected synchronized boolean onDataChannelReceived(final DataChannel dataChannel) {
                Log.v(TAG, "[" + mLabel + "] data channel received: " + dataChannel);
                mReceivedDataChannels.add(dataChannel);
                if (mDataChannelLatch != null) {
                    mDataChannelLatch.countDown();
                }
                return true;
            }

            @Override
            protected void setDataChannelDelegate(final DataChannelDelegate dataChannelDelegate) {
                mDataChannelDelegate = dataChannelDelegate;
                Log.v(TAG, "[" + mLabel + "] data channel delegate set: " + dataChannelDelegate);
            }

            @Override
            public void setStreamMode(final StreamMode mode) {
                mStreamMode = mode;
                if (mModeSetLatch != null) {
                    mModeSetLatch.countDown();
                    mModeSetLatch = null;
                }
            }

            public void waitUntilActive(final CountDownLatch latch) {
                mModeSetLatch = latch;
                if (mStreamMode != null) {
                    mModeSetLatch.countDown();
                    mModeSetLatch = null;
                }
            }
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
                Log.v(TAG, "[" + mLabel + "] got remote source for " + getId() + " : " + mediaSource);
                mRemoteSource = mediaSource;
                if (mCountDownLatch != null) {
                    mCountDownLatch.countDown();
                }
            }

            @Override
            protected void setMediaSourceDelegate(final MediaSourceDelegate mediaSourceDelegate) {
                mMediaSourceDelegate = mediaSourceDelegate;
                if (mLocalSource != null && mediaSourceDelegate != null) {
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
