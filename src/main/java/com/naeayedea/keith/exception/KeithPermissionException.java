package com.naeayedea.keith.exception;

public class KeithPermissionException extends KeithException {

    public KeithPermissionException(String message) {
        super(message);
    }

    public KeithPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeithPermissionException(Throwable cause) {
        super(cause);
    }

    public KeithPermissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
