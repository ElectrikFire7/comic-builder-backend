package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.response.GenerationResponse;
import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.fullcomicbuilder.backend.service.AgentService;
import com.fullcomicbuilder.backend.service.ApiKeyService;
import com.fullcomicbuilder.backend.service.ProjectService;
import com.fullcomicbuilder.backend.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/generate")
public class GenerationController {

    private final AgentService   agentService;

    public GenerationController(AgentService agentService) {
        this.agentService   = agentService;
    }

    /**
     * Generates a character image using the CharacterAgent.
     * Uses the user's active Gemini API key.
     */
    @PostMapping("/character")
    public ResponseEntity<GenerationResponse> generateCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @RequestParam(value = "referenceImages", required = false) MultipartFile[] referenceImages,
            @RequestParam(value = "currentImage", required = false) MultipartFile currentImage,
            @RequestParam("prompt") String prompt,
            @RequestParam("model") String modelName,
            @RequestParam("width") int width,
            @RequestParam("height") int height
    ) {
        // Pass null if referenceImages is null or empty
        MultipartFile[] refImages = (referenceImages != null && referenceImages.length > 0) ? referenceImages : null;
        
        byte[] generatedImage = agentService.characterGeneration(prompt, refImages, currentImage, principal, projectId, modelName, width, height);
        
        GenerationResponse response = new GenerationResponse();
        response.setImageBase64(Base64.getEncoder().encodeToString(generatedImage));
        
        log.debug("Character generated successfully for project: {}", projectId);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/location")
    public ResponseEntity<GenerationResponse> generateLocation(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @RequestParam(value = "styleImage", required = false) MultipartFile styleImage,
            @RequestParam(value = "sketchTemplate", required = false) MultipartFile sketchTemplate,
            @RequestParam(value = "currentImage", required = false) MultipartFile currentImage,
            @RequestParam("prompt") String prompt,
            @RequestParam("model") String modelName,
            @RequestParam("width") int width,
            @RequestParam("height") int height
    ) {
        byte[] generatedImage = agentService.locationGeneration(prompt, styleImage, sketchTemplate, currentImage, principal, projectId, modelName, width, height);

        GenerationResponse response = new GenerationResponse();
        response.setImageBase64(Base64.getEncoder().encodeToString(generatedImage));

        log.debug("Character generated successfully for project: {}", projectId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/scene")
    public ResponseEntity<GenerationResponse> generateScene(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @RequestParam(value = "styleImage", required = false) MultipartFile styleImage,
            @RequestParam(value = "sketchTemplate", required = false) MultipartFile sketchTemplate,
            @RequestParam(value = "currentImage", required = false) MultipartFile currentImage,
            @RequestParam(value = "characterList", required = false) MultipartFile[] characterList,
            @RequestParam(value = "LocationImage", required = false) MultipartFile locationImage,
            @RequestParam("prompt") String prompt,
            @RequestParam("model") String modelName,
            @RequestParam("width") int width,
            @RequestParam("height") int height
    ) {
        byte[] generatedImage = agentService.sceneGeneration(prompt, styleImage, sketchTemplate, currentImage, characterList, locationImage, principal, projectId, modelName, width, height);

        GenerationResponse response = new GenerationResponse();
        response.setImageBase64(Base64.getEncoder().encodeToString(generatedImage));

        log.debug("Character generated successfully for project: {}", projectId);

        return ResponseEntity.ok(response);
    }
}
