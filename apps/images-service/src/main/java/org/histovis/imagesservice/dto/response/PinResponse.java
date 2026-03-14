package org.histovis.imagesservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.histovis.imagesservice.dto.PinDto;

@Data
@AllArgsConstructor
public class PinResponse {
    private PinDto pin;
}
