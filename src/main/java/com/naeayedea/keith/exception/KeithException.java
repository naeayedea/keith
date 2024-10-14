package com.naeayedea.keith.exception;

class KeithException extends Exception {

    public KeithException(String message) {
        super(message);
    }

    public KeithException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeithException(Throwable cause) {
        super(cause);
    }

    public KeithException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
