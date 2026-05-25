package com.fullcomicbuilder.backend.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

import static com.fullcomicbuilder.backend.common.Constants.categories;

@Slf4j
public class GenerationUtils {

    public static void addReferenceImagesToPartsArray(MultipartFile[] referenceImages, JsonArray partsArray){
        for (int i = 0; i < referenceImages.length; i++) {
            try {
                // Add reference image name
                JsonObject refNamePart = new JsonObject();
                refNamePart.addProperty("text", "Reference Image: " + referenceImages[i].getOriginalFilename());
                partsArray.add(refNamePart);

                // Add reference image data
                JsonObject refImagePart = new JsonObject();
                JsonObject inlineData = new JsonObject();
                inlineData.addProperty("mimeType", "image/png");
                inlineData.addProperty("data", Base64.getEncoder().encodeToString(referenceImages[i].getBytes()));
                refImagePart.add("inlineData", inlineData);
                partsArray.add(refImagePart);
            } catch (Exception e) {
                log.error("Error processing reference image: {}", e.getMessage());
            }
        }
    }

    public static void addCharacterImagesToPartsArray(MultipartFile[] referenceImages, JsonArray partsArray){
        for (int i = 0; i < referenceImages.length; i++) {
            try {
                // Add reference image name
                JsonObject refNamePart = new JsonObject();
                refNamePart.addProperty("text", "Character Image: " + referenceImages[i].getOriginalFilename());
                partsArray.add(refNamePart);

                // Add reference image data
                JsonObject refImagePart = new JsonObject();
                JsonObject inlineData = new JsonObject();
                inlineData.addProperty("mimeType", "image/png");
                inlineData.addProperty("data", Base64.getEncoder().encodeToString(referenceImages[i].getBytes()));
                refImagePart.add("inlineData", inlineData);
                partsArray.add(refImagePart);
            } catch (Exception e) {
                log.error("Error processing reference image: {}", e.getMessage());
            }
        }
    }

    public static void addSingleImageToPartsArray(MultipartFile image, JsonArray partsArray, String imageName){
        try {
            // Add current image name
            JsonObject currentNamePart = new JsonObject();
            currentNamePart.addProperty("text",  imageName + ": " + image.getOriginalFilename());
            partsArray.add(currentNamePart);

            // Add current image data
            JsonObject currentImagePart = new JsonObject();
            JsonObject currentInlineData = new JsonObject();
            currentInlineData.addProperty("mimeType", "image/png");
            currentInlineData.addProperty("data", Base64.getEncoder().encodeToString(image.getBytes()));
            currentImagePart.add("inlineData", currentInlineData);
            partsArray.add(currentImagePart);
        } catch (Exception e) {
            log.error("Error processing current image: {}", e.getMessage());
        }
    }

    public static void generationConfigBuilder(JsonObject requestBody) {
//        // 1. Image Specific Parameters
//        JsonObject parameters = new JsonObject();
//        parameters.addProperty("sampleCount", 1); // Note: Google often uses 'sampleCount' instead of 'numImages'
//
//        // Most Google Image APIs use aspect ratio or specific parameter blocks
//        // If your specific endpoint supports width/height, they go here:
//        parameters.addProperty("width", width);
//        parameters.addProperty("height", height);

//        // 2. Generation Config (Only for text/governance settings)
//        JsonObject generationConfig = new JsonObject();
//        generationConfig.addProperty("outputMimeType", "image/png");

        // 3. Safety Settings (Usually a root-level array)
        JsonArray safetySettings = new JsonArray();
        for (String cat : categories) {
            JsonObject obj = new JsonObject();
            obj.addProperty("category", cat);
            obj.addProperty("threshold", "BLOCK_NONE");
            safetySettings.add(obj);
        }

//        requestBody.add("generationConfig", generationConfig);
        requestBody.add("safetySettings", safetySettings);
    }
}
