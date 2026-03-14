package org.histovis.imagesservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.histovis.imagesservice.dto.PinDto;

import java.util.List;

@Data
@AllArgsConstructor
public class DeletePinResponse {
    private boolean deleted;
    private List<PinDto> pins;
}
