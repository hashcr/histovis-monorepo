package org.histovis.imagesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UploadImageResponse {
    private UUID id;
    private String url;
}
