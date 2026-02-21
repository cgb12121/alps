package me.mb.alps.domain.exception;

/**
 * Thrown when a domain invariant is violated (e.g. invalid state transition).
 * Application layer may catch and map to 400 Bad Request.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
