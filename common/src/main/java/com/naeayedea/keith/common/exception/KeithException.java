package com.naeayedea.keith.common.exception;

import org.slf4j.helpers.MessageFormatter;

public class KeithException extends Exception {

    public KeithException(String message) {
        super(message);
    }

    public KeithException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeithException(Throwable cause) {
        super(cause);
    }

    public KeithException(String template, Object ...arguments) {
        super(MessageFormatter.arrayFormat(template, arguments).getMessage());
    }

    public KeithException(String template, Object[] arguments, Throwable cause) {
        super(MessageFormatter.arrayFormat(template, arguments).getMessage(), cause);
    }
}
