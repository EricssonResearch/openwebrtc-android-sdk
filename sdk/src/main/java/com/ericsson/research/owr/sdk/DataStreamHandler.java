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

import com.ericsson.research.owr.DataChannel;
import com.ericsson.research.owr.DataSession;
import com.ericsson.research.owr.Session;

import java.util.ArrayList;
import java.util.List;

class DataStreamHandler extends StreamHandler implements DataSession.OnDataChannelRequestedListener, StreamSet.DataChannelDelegate {
    private static final String TAG = "DataStreamHandler";

    private static final int BASE_PORT = 5000;

    private final List<DataChannel> mDataChannels = new ArrayList<>();

    private Session.DtlsKeyChangeListener mDtlsKeyChangeListener = new Session.DtlsKeyChangeListener() {
        @Override
        public void onDtlsKeyChanged(final String s) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    getStream().setStreamMode(StreamMode.SEND_RECEIVE);
                }
            });
            if (getSession() != null) {
                getSession().removeDtlsKeyChangeListener(this);
            }
            mDtlsKeyChangeListener = null;
        }
    };

    public DataStreamHandler(int index, StreamDescription streamDescription, StreamSet.DataStream dataStream) {
        super(index, streamDescription, dataStream);
        if (dataStream == null) {
            return;
        }
        getDataSession().addOnDataChannelRequestedListener(this);
        getDataSession().addDtlsKeyChangeListener(mDtlsKeyChangeListener);
        getDataStream().setDataChannelDelegate(this);

        boolean haveRemoteDescription = getRemoteStreamDescription() != null;

        StreamMode mode;
        String appLabel;
        int localPort = BASE_PORT + index;
        int streamCount;

        if (!haveRemoteDescription) {
            mode = StreamMode.SEND_RECEIVE;
            appLabel = "webrtc-datachannel";
            streamCount = 1024;
        } else {
            if (getRemoteStreamDescription().getMode() != StreamMode.INACTIVE) {
                mode = StreamMode.SEND_RECEIVE;
            } else {
                mode = StreamMode.INACTIVE;
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        getStream().setStreamMode(StreamMode.INACTIVE);
                    }
                });
            }
            appLabel = getRemoteStreamDescription().getAppLabel();
            int remotePort = getRemoteStreamDescription().getSctpPort();

            streamCount = getRemoteStreamDescription().getSctpStreamCount();

            getDataSession().setSctpRemotePort(remotePort);
        }

        getLocalStreamDescription().setMode(mode);
        getLocalStreamDescription().setAppLabel(appLabel);
        getLocalStreamDescription().setSctpPort(localPort);
        getLocalStreamDescription().setSctpStreamCount(streamCount);
        getDataSession().setSctpLocalPort(localPort);
    }

    public DataSession getDataSession() {
        return (DataSession) getSession();
    }

    public StreamSet.DataStream getDataStream() {
        return (StreamSet.DataStream) getStream();
    }

    @Override
    Session createSession(final boolean isDtlsClient) {
        return new DataSession(isDtlsClient);
    }

    @Override
    public void setRemoteStreamDescription(StreamDescription streamDescription) {
        super.setRemoteStreamDescription(streamDescription);
        StreamMode mode;
        if (getRemoteStreamDescription().getMode() != StreamMode.SEND_RECEIVE) {
            mode = StreamMode.INACTIVE;
        } else {
            mode = StreamMode.SEND_RECEIVE;
        }
        getLocalStreamDescription().setMode(mode);
        getStream().setStreamMode(mode);
        if (mode == StreamMode.INACTIVE) {
            return;
        }
        int remotePort = getRemoteStreamDescription().getSctpPort();
        int streamCount = getRemoteStreamDescription().getSctpStreamCount();

        if (streamCount > 0) {
            getLocalStreamDescription().setSctpStreamCount(streamCount);
        }

        getDataSession().setSctpRemotePort(remotePort);
    }

    @Override
    public void stop() {
        if (getDataSession() != null) {
            getDataSession().removeOnDataChannelRequestedListener(this);
        }
        if (getDataStream() != null) {
            getDataStream().setDataChannelDelegate(null);
        }
        for (DataChannel dataChannel : mDataChannels) {
            dataChannel.close();
        }
        mDataChannels.clear();
        super.stop();
    }

    @Override
    public void onDataChannelRequested(boolean ordered, int max_packet_life_time, int max_retransmits, String protocol, boolean negotiated, int id, String label) {
        Log.d(TAG, "DATACHANNEL requested:" +
                " ordered=" + ordered +
                " max_packet_life_time=" + max_packet_life_time +
                " max_retransmits=" + max_retransmits +
                " protocol=" + protocol +
                " negotiated=" + negotiated +
                " id=" + id +
                " label=" + label);

        final DataChannel dataChannel = new DataChannel(ordered, max_packet_life_time, max_retransmits, protocol, negotiated, (short) id, label);

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean keep = false;

                if (getDataStream() != null) {
                    keep = getDataStream().onDataChannelReceived(dataChannel);
                }

                if (keep) {
                    Log.d(TAG, "adding datachannel to session: " + dataChannel);
                    getDataSession().addDataChannel(dataChannel);
                }
            }
        });
    }

    @Override
    public void addDataChannel(final DataChannel dataChannel) {
        if (getDataSession() != null) {
            getDataSession().addDataChannel(dataChannel);
        }
    }
}
