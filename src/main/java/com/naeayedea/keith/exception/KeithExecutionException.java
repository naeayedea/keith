package com.naeayedea.keith.exception;

public class KeithExecutionException extends KeithException {

    public KeithExecutionException(String message) {
        super(message);
    }

    public KeithExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeithExecutionException(Throwable cause) {
        super(cause);
    }

    public KeithExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

