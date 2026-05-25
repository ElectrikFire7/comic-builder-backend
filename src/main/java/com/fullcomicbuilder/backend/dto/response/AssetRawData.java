package com.fullcomicbuilder.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetRawData {
    private String imageBase64;  // Base64 encoded PNG
    private Object metadata;     // Plain JSON (auto-serialized by Jackson)
}