package com.naeayedea.keith.common.exception;

public class KeithInternalException extends KeithException {

    public KeithInternalException(String message) {
        super(message);
    }

    public KeithInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeithInternalException(Throwable cause) {
        super(cause);
    }

    public KeithInternalException(String template, Object... arguments) {
        super(template, arguments);
    }

    public KeithInternalException(String template, Object[] arguments, Throwable cause) {
        super(template, arguments, cause);
    }
}
