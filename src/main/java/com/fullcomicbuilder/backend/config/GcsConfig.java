package com.fullcomicbuilder.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcsConfig {

    @Value("${app.gcs.bucket}")
    private String bucketName;

    /**
     * Uses Application Default Credentials (ADC).
     * On Cloud Run, the service account attached to the Cloud Run service
     * is automatically used — no credential files or env vars needed.
     */
    @Bean
    public Storage gcsStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    @Bean
    public String gcsBucketName() {
        return bucketName;
    }
}
