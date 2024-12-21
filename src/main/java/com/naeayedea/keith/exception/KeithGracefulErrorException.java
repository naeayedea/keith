package com.naeayedea.keith.exception;

public class KeithGracefulErrorException extends KeithException {

    public KeithGracefulErrorException(String message) {
        super(message);
    }

    public KeithGracefulErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeithGracefulErrorException(Throwable cause) {
        super(cause);
    }

    public KeithGracefulErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
