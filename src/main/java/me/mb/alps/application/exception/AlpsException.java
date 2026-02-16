package me.mb.alps.application.exception;

import lombok.Getter;

/**
 * Base runtime exception for the application layer. Handlers in infrastructure map to HTTP status.
 */
@Getter
public class AlpsException extends RuntimeException {

    private final String errorCode;

    public AlpsException(String message) {
        super(message);
        this.errorCode = null;
    }

    public AlpsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AlpsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }
}
