package org.histovis.imagesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private UUID id;
    private String fileName;
    private String url;
    private String title;
    private String description;
    private List<String> tagsList;
    private List<CommentDto> comments;
    private String notes;
    private String viewableImageUrl;
    private String previewImageUrl;
    private Double resolutionMpp;
    private Integer magnification;
}
