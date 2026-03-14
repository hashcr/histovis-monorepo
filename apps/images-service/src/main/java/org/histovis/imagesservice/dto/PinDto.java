package org.histovis.imagesservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinDto {
    private UUID id;
    private Boolean isPublic;
    private String email;

    @NotNull(message = "x coordinate is required")
    private Double x;

    @NotNull(message = "y coordinate is required")
    private Double y;

    private Double zoom;

    @JsonProperty("sequence_id")
    private Integer sequenceId;

    private String text;
}
