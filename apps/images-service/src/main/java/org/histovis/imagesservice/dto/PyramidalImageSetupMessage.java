package org.histovis.imagesservice.dto;

import java.util.UUID;

public record PyramidalImageSetupMessage(
        UUID imageId,
        String imageUrl
) {}
