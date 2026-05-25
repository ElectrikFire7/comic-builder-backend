package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.request.CharacterMetadataRequest;
import com.fullcomicbuilder.backend.dto.response.AssetRawData;
import com.fullcomicbuilder.backend.dto.response.AssetItem;
import com.fullcomicbuilder.backend.model.Project;
import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.fullcomicbuilder.backend.service.AssetService;
import com.fullcomicbuilder.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/characters")
public class CharacterController {

    private final AssetService   assetService;
    private final ProjectService projectService;

    public CharacterController(AssetService assetService, ProjectService projectService) {
        this.assetService   = assetService;
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<AssetItem>> listCharacters(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId) {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Characters";
        return ResponseEntity.ok(assetService.listAssets(folderPath));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @RequestPart("image") MultipartFile image,
            @RequestPart("metadata") @Valid CharacterMetadataRequest metadata) throws IOException {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Characters";
        assetService.saveAsset(folderPath, metadata.getName(), image, metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Character saved"));
    }

    @PutMapping(value = "/{charName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> editCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String charName,
            @RequestPart("image") MultipartFile image,
            @RequestPart("metadata") @Valid CharacterMetadataRequest metadata) throws IOException {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Characters";
        // Save overwrites existing files
        assetService.saveAsset(folderPath, charName, image, metadata);
        return ResponseEntity.ok(Map.of("message", "Character updated"));
    }

    @DeleteMapping("/{charName}")
    public ResponseEntity<Map<String, String>> deleteCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String charName) {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Characters";
        assetService.deleteAsset(folderPath, charName);
        return ResponseEntity.ok(Map.of("message", "Character deleted"));
    }

    @GetMapping("/char/{charactername}")
    public ResponseEntity<AssetRawData> getCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String charactername) throws IOException {

        // 1. Verify ownership and get project context
        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);

        // 2. Define the character folder path
        String folderPath = project.getStorageFolderPath() + "/Characters";

        AssetRawData assets = assetService.fetchAsset(folderPath, charactername);

        return ResponseEntity.ok(assets);
    }
}
