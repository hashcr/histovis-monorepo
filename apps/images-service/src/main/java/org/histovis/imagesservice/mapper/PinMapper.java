package org.histovis.imagesservice.mapper;

import org.histovis.imagesservice.dto.PinDto;
import org.histovis.imagesservice.model.Pin;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PinMapper {

    public PinDto toDto(Pin pin) {
        return new PinDto(
                pin.getId(),
                pin.getIsPublic(),
                pin.getOwnerEmail(),
                pin.getX(),
                pin.getY(),
                pin.getZoom(),
                pin.getSequenceId(),
                pin.getText()
        );
    }

    public List<PinDto> toDtoList(List<Pin> pins) {
        return pins.stream().map(this::toDto).toList();
    }
}
