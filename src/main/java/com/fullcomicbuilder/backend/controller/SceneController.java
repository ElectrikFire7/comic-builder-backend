package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.request.SceneMetadataRequest;
import com.fullcomicbuilder.backend.dto.response.AssetItem;
import com.fullcomicbuilder.backend.dto.response.AssetRawData;
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
@RequestMapping("/api/v1/projects/{projectId}/scenes")
public class SceneController {

    private final AssetService   assetService;
    private final ProjectService projectService;

    public SceneController(AssetService assetService, ProjectService projectService) {
        this.assetService   = assetService;
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<AssetItem>> listScenes(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId) {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Scenes";
        return ResponseEntity.ok(assetService.listAssets(folderPath));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveScene(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @RequestPart("image") MultipartFile image,
            @RequestPart("metadata") @Valid SceneMetadataRequest metadata) throws IOException {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Scenes";
        assetService.saveAsset(folderPath, metadata.getName(), image, metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Scene saved"));
    }

    @PutMapping(value = "/{sceneName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> editScene(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String sceneName,
            @RequestPart("image") MultipartFile image,
            @RequestPart("metadata") @Valid SceneMetadataRequest metadata) throws IOException {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Scenes";
        assetService.saveAsset(folderPath, sceneName, image, metadata);
        return ResponseEntity.ok(Map.of("message", "Scene updated"));
    }

    @DeleteMapping("/{sceneName}")
    public ResponseEntity<Map<String, String>> deleteScene(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String sceneName) {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Scenes";
        assetService.deleteAsset(folderPath, sceneName);
        return ResponseEntity.ok(Map.of("message", "Scene deleted"));
    }

    @GetMapping("/scene/{scenename}")
    public ResponseEntity<AssetRawData> getCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String scenename) throws IOException {

        // 1. Verify ownership and get project context
        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);

        // 2. Define the character folder path
        String folderPath = project.getStorageFolderPath() + "/Scenes";

        AssetRawData assets = assetService.fetchAsset(folderPath, scenename);

        return ResponseEntity.ok(assets);
    }
}
