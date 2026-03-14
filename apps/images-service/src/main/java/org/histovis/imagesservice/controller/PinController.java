package org.histovis.imagesservice.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.histovis.imagesservice.dto.PinDto;
import org.histovis.imagesservice.dto.CreatePinRequest;
import org.histovis.imagesservice.dto.UpdatePinRequest;
import org.histovis.imagesservice.dto.response.DeletePinResponse;
import org.histovis.imagesservice.dto.response.PinListResponse;
import org.histovis.imagesservice.dto.response.PinResponse;
import org.histovis.imagesservice.service.PinService;
import org.histovis.imagesservice.utils.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(Constants.IMAGES_BASE_URL + Constants.PINS_URL)
public class PinController {

    private final PinService pinService;

    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @GetMapping
    public PinListResponse getPins(@PathVariable UUID id, Authentication authentication) {
        String currentUser = authentication.getName();
        List<PinDto> pins = pinService.getPins(id, currentUser);
        return new PinListResponse(pins);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PinResponse createPin(@PathVariable UUID id,
                                 @RequestBody @Valid CreatePinRequest request,
                                 Authentication authentication) {
        String currentUser = authentication.getName();
        log.info("Create pin for imageId={} by user={}", id, currentUser);
        PinDto pin = pinService.createPin(id, request.getPin(), currentUser);
        return new PinResponse(pin);
    }

    @PutMapping("/{pinId}")
    public PinResponse updatePin(@PathVariable UUID id,
                                 @PathVariable UUID pinId,
                                 @RequestBody @Valid UpdatePinRequest request,
                                 Authentication authentication) {
        log.info("Update pin={} for imageId={} by user={}", pinId, id, authentication.getName());
        PinDto pin = pinService.updatePin(id, pinId, request.getPin());
        return new PinResponse(pin);
    }

    @DeleteMapping("/{pinId}")
    public DeletePinResponse deletePin(@PathVariable UUID id,
                                       @PathVariable UUID pinId,
                                       Authentication authentication) {
        log.info("Delete pin={} for imageId={} by user={}", pinId, id, authentication.getName());
        pinService.deletePin(id, pinId);
        return new DeletePinResponse(true, List.of());
    }
}
