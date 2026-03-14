package org.histovis.imagesservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.histovis.imagesservice.dto.PinDto;

import java.util.List;

@Data
@AllArgsConstructor
public class PinListResponse {
    private List<PinDto> pins;
}
