package org.histovis.imagesservice.mapper;

import org.histovis.imagesservice.dto.CommentDto;
import org.histovis.imagesservice.dto.ImageDto;
import org.histovis.imagesservice.model.Comment;
import org.histovis.imagesservice.model.Image;
import org.histovis.imagesservice.model.ImageTag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageMapper {

    public ImageDto toDto(Image image) {
        List<String> tagsList = image.getTags().stream()
                .map(ImageTag::getTag)
                .toList();

        List<CommentDto> comments = image.getComments().stream()
                .map(this::toCommentDto)
                .toList();

        return new ImageDto(
                image.getId(),
                image.getFileName(),
                image.getPublicUrl(),
                image.getTitle(),
                image.getDescription(),
                tagsList,
                comments,
                image.getNotes(),
                image.getViewableImageUrl(),
                image.getPreviewImageUrl(),
                image.getResolutionMpp(),
                image.getMagnification()
        );
    }

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getEmail(), comment.getAuthor(), comment.getText());
    }

    public List<ImageDto> toDtoList(List<Image> images) {
        return images.stream().map(this::toDto).toList();
    }
}
