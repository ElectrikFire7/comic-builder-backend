package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.request.LocationMetadataRequest;
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
@RequestMapping("/api/v1/projects/{projectId}/locations")
public class LocationController {

    private final AssetService   assetService;
    private final ProjectService projectService;

    public LocationController(AssetService assetService, ProjectService projectService) {
        this.assetService   = assetService;
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<AssetItem>> listLocations(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId) {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Locations";
        return ResponseEntity.ok(assetService.listAssets(folderPath));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> saveLocation(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @RequestPart("image") MultipartFile image,
            @RequestPart("metadata") @Valid LocationMetadataRequest metadata) throws IOException {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Locations";
        assetService.saveAsset(folderPath, metadata.getName(), image, metadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Location saved"));
    }

    @PutMapping(value = "/{locName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> editLocation(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String locName,
            @RequestPart("image") MultipartFile image,
            @RequestPart("metadata") @Valid LocationMetadataRequest metadata) throws IOException {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Locations";
        assetService.saveAsset(folderPath, locName, image, metadata);
        return ResponseEntity.ok(Map.of("message", "Location updated"));
    }

    @DeleteMapping("/{locName}")
    public ResponseEntity<Map<String, String>> deleteLocation(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String locName) {

        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);
        String folderPath = project.getStorageFolderPath() + "/Locations";
        assetService.deleteAsset(folderPath, locName);
        return ResponseEntity.ok(Map.of("message", "Location deleted"));
    }

    @GetMapping("/loc/{locationname}")
    public ResponseEntity<AssetRawData> getCharacter(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @PathVariable String locationname) throws IOException {

        // 1. Verify ownership and get project context
        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);

        // 2. Define the location folder path
        String folderPath = project.getStorageFolderPath() + "/Locations";

        AssetRawData assets = assetService.fetchAsset(folderPath, locationname);

        return ResponseEntity.ok(assets);
    }
}
