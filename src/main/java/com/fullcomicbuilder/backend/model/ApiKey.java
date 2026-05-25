package com.fullcomicbuilder.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "api_keys")
public class ApiKey {

    @Id
    private String id;

    /** References users._id */
    private String userId;

    private String apiKeyName;

    /** AES-256-GCM encrypted value stored as Base64(IV + CipherText) */
    private String apiKeyValue;
}
