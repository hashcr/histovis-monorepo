package org.histovis.imagesservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PatchImageUrlsRequest(
        @NotBlank String viewableImageUrl,
        @NotBlank String previewImageUrl
) {}
