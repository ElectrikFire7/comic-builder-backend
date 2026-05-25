package com.fullcomicbuilder.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationMetadataRequest {
    @NotBlank(message = "Location name is required")
    private String name;

    private String description;
}
