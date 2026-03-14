package org.histovis.imagesservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePinRequest {

    @NotNull
    @Valid
    private PinDto pin;
}
