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

import junit.framework.TestCase;

public class MiscTests extends TestCase {
    public void testStreamMode() {
        assertSame(StreamMode.INACTIVE, StreamMode.get(false, false));
        assertSame(StreamMode.SEND_ONLY, StreamMode.get(true, false));
        assertSame(StreamMode.RECEIVE_ONLY, StreamMode.get(false, true));
        assertSame(StreamMode.SEND_RECEIVE, StreamMode.get(true, true));

        assertSame(StreamMode.INACTIVE, StreamMode.INACTIVE.reverse(false, false));
        assertSame(StreamMode.INACTIVE, StreamMode.INACTIVE.reverse(true, false));
        assertSame(StreamMode.INACTIVE, StreamMode.INACTIVE.reverse(false, true));
        assertSame(StreamMode.INACTIVE, StreamMode.INACTIVE.reverse(true, true));

        assertSame(StreamMode.INACTIVE, StreamMode.SEND_ONLY.reverse(false, false));
        assertSame(StreamMode.INACTIVE, StreamMode.SEND_ONLY.reverse(true, false));
        assertSame(StreamMode.RECEIVE_ONLY, StreamMode.SEND_ONLY.reverse(false, true));
        assertSame(StreamMode.RECEIVE_ONLY, StreamMode.SEND_ONLY.reverse(true, true));

        assertSame(StreamMode.INACTIVE, StreamMode.RECEIVE_ONLY.reverse(false, false));
        assertSame(StreamMode.SEND_ONLY, StreamMode.RECEIVE_ONLY.reverse(true, false));
        assertSame(StreamMode.INACTIVE, StreamMode.RECEIVE_ONLY.reverse(false, true));
        assertSame(StreamMode.SEND_ONLY, StreamMode.RECEIVE_ONLY.reverse(true, true));

        assertSame(StreamMode.INACTIVE, StreamMode.SEND_RECEIVE.reverse(false, false));
        assertSame(StreamMode.SEND_ONLY, StreamMode.SEND_RECEIVE.reverse(true, false));
        assertSame(StreamMode.RECEIVE_ONLY, StreamMode.SEND_RECEIVE.reverse(false, true));
        assertSame(StreamMode.SEND_RECEIVE, StreamMode.SEND_RECEIVE.reverse(true, true));
    }
}
