package org.histovis.analysisservice.dto;

import java.util.UUID;

public record PyramidalImageSetupMessage(
        UUID imageId,
        String imageUrl
) {}
