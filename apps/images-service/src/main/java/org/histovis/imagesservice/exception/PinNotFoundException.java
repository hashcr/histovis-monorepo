package org.histovis.imagesservice.exception;

public class PinNotFoundException extends RuntimeException {

    public PinNotFoundException(String pinId) {
        super("Pin not found: " + pinId);
    }
}
