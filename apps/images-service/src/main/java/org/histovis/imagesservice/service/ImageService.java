package org.histovis.imagesservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.histovis.imagesservice.dto.CommentDto;
import org.histovis.imagesservice.dto.ImageDto;
import org.histovis.imagesservice.dto.UploadImageResponse;
import org.histovis.imagesservice.exception.ImageNotFoundException;
import org.histovis.imagesservice.mapper.ImageMapper;
import org.histovis.imagesservice.model.Comment;
import org.histovis.imagesservice.model.Image;
import org.histovis.imagesservice.model.ImageTag;
import org.histovis.imagesservice.repository.ImageRepository;
import org.histovis.imagesservice.storage.ObjectStorageService;
import org.histovis.imagesservice.config.RabbitMQConfig;
import org.histovis.imagesservice.dto.PyramidalImageSetupMessage;
import org.histovis.imagesservice.utils.Constants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final ObjectStorageService storageService;
    private final ImageMapper imageMapper;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public ImageService(ImageRepository imageRepository,
                        ObjectStorageService storageService,
                        ImageMapper imageMapper,
                        ObjectMapper objectMapper,
                        RabbitTemplate rabbitTemplate) {
        this.imageRepository = imageRepository;
        this.storageService = storageService;
        this.imageMapper = imageMapper;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(readOnly = true)
    public List<ImageDto> searchImages(String query, List<String> tagsList) {
        List<Image> images = imageRepository.searchByText(query);

        if (tagsList != null && !tagsList.isEmpty()) {
            List<String> normalizedFilter = tagsList.stream().map(String::toLowerCase).toList();
            images = images.stream()
                    .filter(image -> {
                        List<String> imageTags = image.getTags().stream()
                                .map(t -> t.getTag().toLowerCase())
                                .toList();
                        return imageTags.containsAll(normalizedFilter);
                    })
                    .toList();
        }

        return imageMapper.toDtoList(images);
    }

    @Transactional(readOnly = true)
    public ImageDto getImage(UUID id) {
        Image image = imageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ImageNotFoundException(id.toString()));
        return imageMapper.toDto(image);
    }

    public UploadImageResponse uploadImage(String fileName,
                                           String title,
                                           String description,
                                           String tagsListJson,
                                           MultipartFile imageFile,
                                           String uploadedBy) {
        List<String> tagsList = parseTagsList(tagsListJson);

        String storageKey = Constants.STORAGE_PREFIX + UUID.randomUUID() + "/" + fileName;

        log.info("Uploading image: fileName={}, uploadedBy={}", fileName, uploadedBy);

        String publicUrl;
        try {
            publicUrl = storageService.upload(
                    storageKey,
                    imageFile.getInputStream(),
                    imageFile.getSize(),
                    imageFile.getContentType()
            );
        } catch (IOException e) {
            log.error("Failed to read image file for upload: {}", fileName, e);
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        boolean isSvs = fileName.toLowerCase().endsWith(".svs");

        Image image = new Image();
        image.setFileName(fileName);
        image.setStorageKey(storageKey);
        image.setPublicUrl(publicUrl);
        image.setViewableImageUrl(isSvs ? null : publicUrl);
        image.setPreviewImageUrl(isSvs ? null : publicUrl);
        image.setTitle(title);
        image.setDescription(description);
        image.setCreatedBy(uploadedBy);

        setTags(image, tagsList);

        Image saved = imageRepository.save(image);
        log.info("Image uploaded successfully: id={}, storageKey={}, isSvs={}", saved.getId(), storageKey, isSvs);

        if (isSvs) {
            PyramidalImageSetupMessage msg = new PyramidalImageSetupMessage(saved.getId(), publicUrl);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TILESERVER_SETUP_WSI_ROUTING_KEY, msg);
            log.info("PyramidalImageSetup dispatched for SVS: imageId={}", saved.getId());
        }

        return new UploadImageResponse(saved.getId(), saved.getPublicUrl());
    }

    public ImageDto updateImage(UUID id,
                                String fileName,
                                String title,
                                String description,
                                String tagsListJson,
                                String notes,
                                String commentsJson,
                                MultipartFile imageFile) {
        Image image = imageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ImageNotFoundException(id.toString()));

        log.info("Updating image: id={}", id);

        if (fileName != null) image.setFileName(fileName);
        if (title != null) image.setTitle(title);
        if (description != null) image.setDescription(description);
        if (notes != null) image.setNotes(notes);

        if (tagsListJson != null) {
            List<String> tagsList = parseTagsList(tagsListJson);
            setTags(image, tagsList);
        }

        if (commentsJson != null) {
            List<CommentDto> commentDtos = parseComments(commentsJson);
            setComments(image, commentDtos);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String oldStorageKey = image.getStorageKey();
            String newStorageKey = Constants.STORAGE_PREFIX + UUID.randomUUID() + "/" + image.getFileName();

            log.info("Replacing image file: oldKey={}, newKey={}", oldStorageKey, newStorageKey);

            String publicUrl;
            try {
                publicUrl = storageService.upload(
                        newStorageKey,
                        imageFile.getInputStream(),
                        imageFile.getSize(),
                        imageFile.getContentType()
                );
            } catch (IOException e) {
                log.error("Failed to read image file during update: id={}", id, e);
                throw new RuntimeException("Failed to read uploaded file", e);
            }

            storageService.delete(oldStorageKey);

            image.setStorageKey(newStorageKey);
            image.setPublicUrl(publicUrl);
        }

        Image saved = imageRepository.save(image);
        log.info("Image updated successfully: id={}", id);
        return imageMapper.toDto(saved);
    }

    public void patchImageUrls(UUID id, String viewableImageUrl, String previewImageUrl) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException(id.toString()));
        image.setViewableImageUrl(viewableImageUrl);
        image.setPreviewImageUrl(previewImageUrl);
        imageRepository.save(image);
        log.info("Image URLs patched: id={}", id);
    }

    private void setTags(Image image, List<String> tagsList) {
        image.getTags().clear();
        if (tagsList != null) {
            tagsList.forEach(tag -> image.getTags().add(new ImageTag(image, tag.toLowerCase())));
        }
    }

    private void setComments(Image image, List<CommentDto> commentDtos) {
        image.getComments().clear();
        if (commentDtos != null) {
            commentDtos.forEach(dto ->
                    image.getComments().add(new Comment(image, dto.getEmail(), dto.getAuthor(), dto.getText())));
        }
    }

    private List<String> parseTagsList(String tagsListJson) {
        if (tagsListJson == null || tagsListJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(tagsListJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Invalid tagsList JSON: {}", tagsListJson);
            throw new IllegalArgumentException("Invalid tagsList format. Expected a JSON array of strings, e.g. [\"tag1\",\"tag2\"]");
        }
    }

    private List<CommentDto> parseComments(String commentsJson) {
        if (commentsJson == null || commentsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(commentsJson, new TypeReference<List<CommentDto>>() {});
        } catch (Exception e) {
            log.warn("Invalid comments JSON: {}", commentsJson);
            throw new IllegalArgumentException("Invalid comments format. Expected a JSON array of comment objects.");
        }
    }
}
