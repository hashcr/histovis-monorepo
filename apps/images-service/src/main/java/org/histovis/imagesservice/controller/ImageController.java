package org.histovis.imagesservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.histovis.imagesservice.dto.ImageDto;
import org.histovis.imagesservice.dto.PatchImageUrlsRequest;
import org.histovis.imagesservice.dto.UploadImageResponse;
import org.histovis.imagesservice.dto.response.ImageListResponse;
import org.histovis.imagesservice.dto.response.ImageResponse;
import org.histovis.imagesservice.service.ImageService;
import org.histovis.imagesservice.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(Constants.IMAGES_BASE_URL)
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/search")
    public ImageListResponse search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tagsList) {

        List<String> tags = null;
        if (tagsList != null && !tagsList.isBlank()) {
            tags = Arrays.stream(tagsList.split(","))
                    .map(String::trim)
                    .filter(t -> !t.isBlank())
                    .toList();
        }

        List<ImageDto> images = imageService.searchImages(query, tags);
        return new ImageListResponse(images);
    }

    @GetMapping("/{id}")
    public ImageResponse getImage(@PathVariable UUID id) {
        return new ImageResponse(imageService.getImage(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadImageResponse uploadImage(
            @RequestParam @NotBlank String fileName,
            @RequestParam @NotBlank String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tagsList,
            @RequestParam(required = false) Integer magnification,
            @RequestParam(required = false) Double resolutionMpp,
            @RequestParam MultipartFile imageFile,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("Upload request from user={}, fileName={}", username, fileName);
        return imageService.uploadImage(fileName, title, description, tagsList, magnification, resolutionMpp, imageFile, username);
    }

    @PatchMapping("/{id}/urls")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchImageUrls(@PathVariable UUID id,
                               @RequestBody @Valid PatchImageUrlsRequest request) {
        log.info("Patch URLs request for imageId={}", id);
        imageService.patchImageUrls(id, request.viewableImageUrl(), request.previewImageUrl());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageResponse updateImage(
            @PathVariable UUID id,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tagsList,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String comments,
            @RequestParam(required = false) MultipartFile imageFile,
            Authentication authentication) {

        log.info("Update request for imageId={} from user={}", id, authentication.getName());
        ImageDto updated = imageService.updateImage(id, fileName, title, description, tagsList, notes, comments, imageFile);
        return new ImageResponse(updated);
    }
}
