package com.fullcomicbuilder.backend.service;

import com.fullcomicbuilder.backend.model.ApiKey;
import com.fullcomicbuilder.backend.model.User;
import com.fullcomicbuilder.backend.repository.ApiKeyRepository;
import com.fullcomicbuilder.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository   userRepository;
    private final EncryptionService encryptionService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                         UserRepository userRepository,
                         EncryptionService encryptionService) {
        this.apiKeyRepository  = apiKeyRepository;
        this.userRepository    = userRepository;
        this.encryptionService = encryptionService;
    }

    public ApiKey addKey(String userId, String name, String rawValue) {
        String encrypted = encryptionService.encrypt(rawValue);
        ApiKey key = ApiKey.builder()
                .userId(userId)
                .apiKeyName(name)
                .apiKeyValue(encrypted)
                .build();
        return apiKeyRepository.save(key);
    }

    public List<ApiKey> listKeys(String userId) {
        return apiKeyRepository.findAllByUserId(userId);
    }

    public void setActive(String userId, String keyId) {
        // Verify the key belongs to this user
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));
        if (!key.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setActiveApiKeyId(keyId);
        userRepository.save(user);
    }

    public void deleteKey(String userId, String keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));
        if (!key.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        apiKeyRepository.deleteById(keyId);

        // If this was the active key, clear it from the user document
        userRepository.findById(userId).ifPresent(user -> {
            if (keyId.equals(user.getActiveApiKeyId())) {
                user.setActiveApiKeyId(null);
                userRepository.save(user);
            }
        });
    }

    /**
     * Returns the decrypted value of the user's active API key.
     * Used internally by the AI generation endpoints.
     */
    public String getDecryptedActiveKey(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getActiveApiKeyId() == null) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "No active API key set. Please add and activate an API key.");
        }

        ApiKey key = apiKeyRepository.findById(user.getActiveApiKeyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Active API key record not found"));

        return encryptionService.decrypt(key.getApiKeyValue());
    }
}
