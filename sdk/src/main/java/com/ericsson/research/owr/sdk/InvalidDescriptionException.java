/*
 * Copyright (C) 2015 Ericsson Research
 * Author: Patrik Oldsberg <patrik.oldsberg@ericsson.com>
 */
package com.ericsson.research.owr.sdk;

public class InvalidDescriptionException extends Exception {
    public InvalidDescriptionException(final String detailMessage) {
        super(detailMessage);
    }

    public InvalidDescriptionException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }
}
