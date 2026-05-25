package com.fullcomicbuilder.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CharacterMetadataRequest {
    @NotBlank(message = "Character name is required")
    private String name;

    private String age;
    private String height;
    private String eyeColour;
    private String hairstyleAndColour;
    private String tshirt;
    private String pant;
    private String boots;
    private String accessories;
}
