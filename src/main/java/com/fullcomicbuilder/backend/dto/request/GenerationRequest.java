package com.fullcomicbuilder.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class GenerationRequest {
    @NotBlank(message = "Prompt is required")
    private String prompt;

    /** GCS object paths of reference images (optional) */
    private List<String> referenceImagePaths;

    /** e.g. "gemini-2.0-flash", "gemini-2.0-flash-exp" */
    @NotBlank(message = "Gemini model is required")
    private String geminiModel;
}
