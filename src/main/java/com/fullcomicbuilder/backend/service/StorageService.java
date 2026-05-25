package com.fullcomicbuilder.backend.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles all Google Cloud Storage operations.
 * Authentication is handled by ADC — no credentials configured here.
 */
@Service
public class StorageService {

    private static final List<String> ASSET_SUBFOLDERS =
            List.of("Characters/", "Locations/", "Scenes/", "Styles/");

    private final Storage storage;
    private final String  bucketName;

    public StorageService(Storage storage, String gcsBucketName) {
        this.storage    = storage;
        this.bucketName = gcsBucketName;
    }

    // -------------------------------------------------------------------------
    // Folder / Structure
    // -------------------------------------------------------------------------

    /**
     * Creates the top-level user folder when a new user signs up.
     * GCS doesn't have real folders — we create a zero-byte placeholder.
     */
    public void createUserFolder(String sanitizedEmail) {
        createPlaceholder(sanitizedEmail + "/");
    }

    /**
     * Creates the project folder with all 4 asset subfolders.
     */
    public void createProjectFolders(String projectPath) {
        createPlaceholder(projectPath + "/");
        for (String sub : ASSET_SUBFOLDERS) {
            createPlaceholder(projectPath + "/" + sub);
        }
    }

    private void createPlaceholder(String path) {
        BlobId   blobId   = BlobId.of(bucketName, path);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/x-directory").build();
        storage.create(blobInfo, new byte[0]);
    }

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    public void uploadFile(String path, byte[] data, String contentType) {
        BlobId   blobId   = BlobId.of(bucketName, path);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        storage.create(blobInfo, data);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    public void deleteObject(String path) {
        storage.delete(BlobId.of(bucketName, path));
    }

    /**
     * Deletes all objects under a given prefix (simulates folder deletion).
     */
    public void deleteFolder(String prefix) {
        Page<Blob> blobs = storage.list(bucketName,
                Storage.BlobListOption.prefix(prefix));
        for (Blob blob : blobs.iterateAll()) {
            blob.delete();
        }
    }

    // -------------------------------------------------------------------------
    // Rename (copy-then-delete — GCS has no native rename)
    // -------------------------------------------------------------------------

    public void renameFolder(String oldPrefix, String newPrefix) {
        Page<Blob> blobs = storage.list(bucketName,
                Storage.BlobListOption.prefix(oldPrefix));

        List<BlobId> toDelete = new ArrayList<>();

        for (Blob blob : blobs.iterateAll()) {
            String newName = newPrefix + blob.getName().substring(oldPrefix.length());
            BlobInfo newBlobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, newName))
                    .setContentType(blob.getContentType())
                    .build();
            storage.copy(Storage.CopyRequest.of(blob.getBlobId(), newBlobInfo)).getResult();
            toDelete.add(blob.getBlobId());
        }

        // Batch delete originals
        if (!toDelete.isEmpty()) {
            storage.delete(toDelete);
        }
    }

    // -------------------------------------------------------------------------
    // Download (for passing reference images to AI services)
    // -------------------------------------------------------------------------

    public byte[] downloadFile(String path) {
        Blob blob = storage.get(BlobId.of(bucketName, path));
        if (blob == null) {
            throw new RuntimeException("File not found in GCS: " + path);
        }
        return blob.getContent();
    }

    // -------------------------------------------------------------------------
    // Signed URLs (for frontend asset retrieval)
    // -------------------------------------------------------------------------

    public String getSignedUrl(String path, Duration duration) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, path)).build();
        URL url = storage.signUrl(blobInfo,
                duration.toMinutes(), TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature());
        return url.toString();
    }

    // -------------------------------------------------------------------------
    // List objects under a prefix
    // -------------------------------------------------------------------------

    public List<String> listObjectNames(String prefix) {
        Page<Blob> blobs = storage.list(bucketName,
                Storage.BlobListOption.prefix(prefix),
                Storage.BlobListOption.currentDirectory());

        List<String> names = new ArrayList<>();
        for (Blob blob : blobs.iterateAll()) {
            String name = blob.getName();
            // Skip the placeholder folder entry itself and non-PNG files
            if (!name.equals(prefix) && name.endsWith(".png")) {
                // Extract just the filename without folder prefix or extension
                String filename = name.substring(prefix.length());
                if (filename.endsWith(".png")) {
                    filename = filename.substring(0, filename.length() - 4);
                }
                names.add(filename);
            }
        }
        return names;
    }
}
