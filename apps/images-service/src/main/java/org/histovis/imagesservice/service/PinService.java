package org.histovis.imagesservice.service;

import lombok.extern.slf4j.Slf4j;
import org.histovis.imagesservice.dto.PinDto;
import org.histovis.imagesservice.exception.ImageNotFoundException;
import org.histovis.imagesservice.exception.PinNotFoundException;
import org.histovis.imagesservice.mapper.PinMapper;
import org.histovis.imagesservice.model.Image;
import org.histovis.imagesservice.model.Pin;
import org.histovis.imagesservice.repository.ImageRepository;
import org.histovis.imagesservice.repository.PinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PinService {

    private final PinRepository pinRepository;
    private final ImageRepository imageRepository;
    private final PinMapper pinMapper;

    public PinService(PinRepository pinRepository,
                      ImageRepository imageRepository,
                      PinMapper pinMapper) {
        this.pinRepository = pinRepository;
        this.imageRepository = imageRepository;
        this.pinMapper = pinMapper;
    }

    @Transactional(readOnly = true)
    public List<PinDto> getPins(UUID imageId, String currentUser) {
        ensureImageExists(imageId);
        List<Pin> pins = pinRepository.findVisiblePins(imageId, currentUser);
        return pinMapper.toDtoList(pins);
    }

    public PinDto createPin(UUID imageId, PinDto pinDto, String currentUser) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId.toString()));

        Pin pin = new Pin();
        pin.setImage(image);
        pin.setOwnerEmail(pinDto.getEmail() != null ? pinDto.getEmail() : currentUser);
        pin.setIsPublic(pinDto.getIsPublic() != null ? pinDto.getIsPublic() : false);
        pin.setX(pinDto.getX());
        pin.setY(pinDto.getY());
        pin.setZoom(pinDto.getZoom());
        pin.setSequenceId(pinDto.getSequenceId());
        pin.setText(pinDto.getText());

        Pin saved = pinRepository.save(pin);
        log.info("Pin created: id={}, imageId={}, owner={}", saved.getId(), imageId, saved.getOwnerEmail());
        return pinMapper.toDto(saved);
    }

    public PinDto updatePin(UUID imageId, UUID pinId, PinDto pinDto) {
        ensureImageExists(imageId);

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new PinNotFoundException(pinId.toString()));

        if (pinDto.getIsPublic() != null) pin.setIsPublic(pinDto.getIsPublic());
        if (pinDto.getEmail() != null) pin.setOwnerEmail(pinDto.getEmail());
        if (pinDto.getX() != null) pin.setX(pinDto.getX());
        if (pinDto.getY() != null) pin.setY(pinDto.getY());
        if (pinDto.getZoom() != null) pin.setZoom(pinDto.getZoom());
        if (pinDto.getSequenceId() != null) pin.setSequenceId(pinDto.getSequenceId());
        if (pinDto.getText() != null) pin.setText(pinDto.getText());

        Pin saved = pinRepository.save(pin);
        log.info("Pin updated: id={}, imageId={}", pinId, imageId);
        return pinMapper.toDto(saved);
    }

    public void deletePin(UUID imageId, UUID pinId) {
        ensureImageExists(imageId);

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new PinNotFoundException(pinId.toString()));

        pinRepository.delete(pin);
        log.info("Pin deleted: id={}, imageId={}", pinId, imageId);
    }

    private void ensureImageExists(UUID imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ImageNotFoundException(imageId.toString());
        }
    }
}
