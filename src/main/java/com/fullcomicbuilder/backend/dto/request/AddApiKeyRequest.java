package com.fullcomicbuilder.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddApiKeyRequest {
    @NotBlank(message = "API key name is required")
    private String apiKeyName;

    @NotBlank(message = "API key value is required")
    private String apiKeyValue;
}
