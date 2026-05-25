package com.fullcomicbuilder.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse {
    /** Base64-encoded PNG of the generated image */
    private String imageBase64;
}
