package org.histovis.imagesservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePinRequest {

    @NotNull
    @Valid
    private PinDto pin;
}
