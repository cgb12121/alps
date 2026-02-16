package me.mb.alps.application.exception;

import lombok.Getter;

import java.util.UUID;

/**
 * Thrown when a required entity is not found. Infrastructure maps to HTTP 404.
 */
@Getter
public class NotFoundException extends AlpsException {

    private final String resource;
    private final UUID id;

    public NotFoundException(String resource, UUID id) {
        super("%s not found: %s".formatted(resource, id));
        this.resource = resource;
        this.id = id;
    }

    public NotFoundException(String resource, String identifier) {
        super("%s not found: %s".formatted(resource, identifier));
        this.resource = resource;
        this.id = null;
    }

}
