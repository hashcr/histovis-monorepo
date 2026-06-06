package org.histovis.imagesservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.histovis.imagesservice.dto.ImageDto;
import org.histovis.imagesservice.dto.UploadImageResponse;
import org.histovis.imagesservice.exception.ImageNotFoundException;
import org.histovis.imagesservice.mapper.ImageMapper;
import org.histovis.imagesservice.model.ImageTag;
import org.histovis.imagesservice.model.Image;
import org.histovis.imagesservice.repository.ImageRepository;
import org.histovis.imagesservice.storage.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageRepository, objectStorageService, imageMapper, new ObjectMapper(), rabbitTemplate);
    }

    @Test
    void uploadImage_shouldSaveImageAndReturnResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "test.tiff", "image/tiff", "fake-content".getBytes());

        when(objectStorageService.upload(anyString(), any(), anyLong(), anyString()))
                .thenReturn("http://minio/bucket/images/uuid/test.tiff");

        Image saved = new Image();
        saved.setId(UUID.randomUUID());
        saved.setPublicUrl("http://minio/bucket/images/uuid/test.tiff");
        saved.setTags(new ArrayList<>());
        saved.setComments(new ArrayList<>());
        when(imageRepository.save(any(Image.class))).thenReturn(saved);

        UploadImageResponse response = imageService.uploadImage(
                "test.tiff", "Test Title", "desc", null, file, "user1");

        assertThat(response.getId()).isEqualTo(saved.getId());
        assertThat(response.getUrl()).isEqualTo(saved.getPublicUrl());
        verify(imageRepository).save(any(Image.class));
        verify(objectStorageService).upload(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void getImage_shouldThrowImageNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(imageRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageService.getImage(id))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void searchImages_shouldFilterByTags() {
        Image withTag = new Image();
        withTag.setId(UUID.randomUUID());
        withTag.setTags(new ArrayList<>());
        withTag.setComments(new ArrayList<>());

        Image withoutTag = new Image();
        withoutTag.setId(UUID.randomUUID());
        withoutTag.setTags(new ArrayList<>());
        withoutTag.setComments(new ArrayList<>());

        when(imageRepository.searchByText(null)).thenReturn(List.of(withTag, withoutTag));
        when(imageMapper.toDtoList(anyList())).thenAnswer(inv -> {
            List<Image> images = inv.getArgument(0);
            return images.stream().map(i -> new ImageDto(i.getId(), null, null, null, null, List.of(), List.of(), null, null, null, null, null)).toList();
        });

        // No tag filter — both returned
        List<ImageDto> all = imageService.searchImages(null, null);
        assertThat(all).hasSize(2);
    }

    @Test
    void searchImages_shouldReturnOnlyImagesMatchingAllTags() {
        Image matchingImage = new Image();
        matchingImage.setId(UUID.randomUUID());
        matchingImage.setTags(List.of(new ImageTag(matchingImage, "history"), new ImageTag(matchingImage, "art")));
        matchingImage.setComments(new ArrayList<>());

        Image partialMatchImage = new Image();
        partialMatchImage.setId(UUID.randomUUID());
        partialMatchImage.setTags(List.of(new ImageTag(partialMatchImage, "history")));
        partialMatchImage.setComments(new ArrayList<>());

        when(imageRepository.searchByText(null)).thenReturn(List.of(matchingImage, partialMatchImage));
        when(imageMapper.toDtoList(List.of(matchingImage))).thenAnswer(inv -> {
            List<Image> images = inv.getArgument(0);
            return images.stream().map(i -> new ImageDto(i.getId(), null, null, null, null, List.of(), List.of(), null, null, null, null, null)).toList();
        });

        List<ImageDto> result = imageService.searchImages(null, List.of("history", "art"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(matchingImage.getId());
    }

    @Test
    void uploadImage_shouldThrow_whenTagsListIsInvalidJson() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "test.tiff", "image/tiff", "fake".getBytes());

        assertThatThrownBy(() -> imageService.uploadImage(
                "test.tiff", "Title", null, "not-valid-json", file, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid tagsList format");
    }
}
