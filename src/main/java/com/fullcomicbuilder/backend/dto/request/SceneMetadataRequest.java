package com.fullcomicbuilder.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SceneMetadataRequest {
    @NotBlank(message = "Scene name is required")
    private String name;

    private String description;
}
