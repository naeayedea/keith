package com.naeayedea.keith.common.exception;

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

    public KeithExecutionException(String template, Object... arguments) {
        super(template, arguments);
    }

    public KeithExecutionException(String template, Object[] arguments, Throwable cause) {
        super(template, arguments, cause);
    }
}

