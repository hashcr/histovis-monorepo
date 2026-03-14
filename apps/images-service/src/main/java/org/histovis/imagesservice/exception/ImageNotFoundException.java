package org.histovis.imagesservice.exception;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(String id) {
        super("Image not found: " + id);
    }
}
