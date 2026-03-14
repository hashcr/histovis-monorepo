package org.histovis.imagesservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.histovis.imagesservice.dto.ImageDto;

import java.util.List;

@Data
@AllArgsConstructor
public class ImageListResponse {
    private List<ImageDto> images;
}
