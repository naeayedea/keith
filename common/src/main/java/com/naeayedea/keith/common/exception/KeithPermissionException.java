package com.naeayedea.keith.common.exception;

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

    public KeithPermissionException(String template, Object... arguments) {
        super(template, arguments);
    }

    public KeithPermissionException(String template, Object[] arguments, Throwable cause) {
        super(template, arguments, cause);
    }
}
