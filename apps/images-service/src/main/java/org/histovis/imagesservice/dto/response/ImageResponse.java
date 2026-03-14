package org.histovis.imagesservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.histovis.imagesservice.dto.ImageDto;

@Data
@AllArgsConstructor
public class ImageResponse {
    private ImageDto image;
}
