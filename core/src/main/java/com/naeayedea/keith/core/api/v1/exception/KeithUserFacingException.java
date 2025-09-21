/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.api.v1.exception;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

public class KeithUserFacingException extends Exception {

    private final HttpStatus status;

    public KeithUserFacingException(@NonNull HttpStatus status, @NonNull String message, Throwable cause) {
        super(message, cause);

        this.status = status;
    }

    public KeithUserFacingException(@NonNull HttpStatus status, @NonNull String message) {
        super(message);

        this.status = status;
    }

    @NonNull
    public HttpStatus getStatus() {
        return status;
    }

}
