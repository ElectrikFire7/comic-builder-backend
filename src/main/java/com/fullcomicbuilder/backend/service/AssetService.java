package com.fullcomicbuilder.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullcomicbuilder.backend.dto.response.AssetRawData;
import com.fullcomicbuilder.backend.dto.response.AssetItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * Shared service for Characters, Locations, and Scenes asset management.
 * Each asset consists of a PNG image and a JSON metadata file in GCS.
 */
@Service
public class AssetService {

    private final StorageService storageService;
    private final ObjectMapper   objectMapper;

    public AssetService(StorageService storageService) {
        this.storageService = storageService;
        this.objectMapper   = new ObjectMapper();
    }

    /**
     * Saves or overwrites an asset (image + metadata JSON) in GCS.
     *
     * @param folderPath  e.g. "username/MyProject/Characters"
     * @param assetName   e.g. "HeroCharacter"
     * @param image       PNG image file
     * @param metadata    Object to serialise as JSON
     */
    public void saveAsset(String folderPath, String assetName,
                          MultipartFile image, Object metadata) throws IOException {
        // Upload PNG
        String imagePath = folderPath + "/" + assetName + ".png";
        storageService.uploadFile(imagePath, image.getBytes(), "image/png");

        // Upload JSON
        String jsonPath = folderPath + "/" + assetName + ".json";
        byte[] jsonBytes = objectMapper.writeValueAsBytes(metadata);
        storageService.uploadFile(jsonPath, jsonBytes, "application/json");
    }

    public AssetRawData fetchAsset(String folderPath, String assetName) throws IOException {
        String imagePath = folderPath + "/" + assetName + ".png";
        String jsonPath = folderPath + "/" + assetName + ".json";

        byte[] imageBytes = storageService.downloadFile(imagePath);
        byte[] jsonBytes = storageService.downloadFile(jsonPath);

        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

        // Convert JSON bytes → Object
        Object metadata = objectMapper.readValue(jsonBytes, Object.class);

        return AssetRawData.builder()
                .imageBase64(imageBase64)
                .metadata(metadata)
                .build();
    }

    /**
     * Deletes both the PNG and JSON for an asset.
     *
     * @param folderPath  e.g. "username/MyProject/Characters"
     * @param assetName   e.g. "HeroCharacter"
     */
    public void deleteAsset(String folderPath, String assetName) {
        storageService.deleteObject(folderPath + "/" + assetName + ".png");
        storageService.deleteObject(folderPath + "/" + assetName + ".json");
    }

    /**
     * Lists all assets in a folder, returning names and signed image URLs.
     *
     * @param folderPath  e.g. "username/MyProject/Characters/"
     */
    public List<AssetItem> listAssets(String folderPath) {
        // Ensure trailing slash for prefix listing
        String prefix = folderPath.endsWith("/") ? folderPath : folderPath + "/";

        List<String> names = storageService.listObjectNames(prefix);
        return names.stream().map(name -> {
            String imagePath = prefix + name + ".png";
            String signedUrl = storageService.getSignedUrl(imagePath, Duration.ofHours(1));
            return AssetItem.builder()
                    .name(name)
                    .imageUrl(signedUrl)
                    .build();
        }).toList();
    }
}
