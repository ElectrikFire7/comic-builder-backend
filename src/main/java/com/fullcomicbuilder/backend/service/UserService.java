package com.fullcomicbuilder.backend.service;

import com.fullcomicbuilder.backend.model.User;
import com.fullcomicbuilder.backend.repository.ApiKeyRepository;
import com.fullcomicbuilder.backend.repository.ProjectRepository;
import com.fullcomicbuilder.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository    userRepository;
    private final ApiKeyRepository  apiKeyRepository;
    private final ProjectRepository projectRepository;
    private final StorageService    storageService;

    public UserService(UserRepository userRepository,
                       ApiKeyRepository apiKeyRepository,
                       ProjectRepository projectRepository,
                       StorageService storageService) {
        this.userRepository    = userRepository;
        this.apiKeyRepository  = apiKeyRepository;
        this.projectRepository = projectRepository;
        this.storageService    = storageService;
    }

    /**
     * Retrieves an existing user by email, or creates a new one.
     * On first creation, the user's GCS root folder is provisioned.
     */
    public User getOrCreateUser(String email, String name) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .username(name)
                    .build();
            User saved = userRepository.save(newUser);

            // Create GCS user root folder
            String sanitized = sanitizeEmail(email);
            storageService.createUserFolder(sanitized);

            return saved;
        });
    }

    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    /**
     * Deletes the user and cascades:
     * 1. All API keys from DB
     * 2. All projects from DB
     * 3. Entire GCS user folder
     * 4. User document
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = findById(userId);

        // 1. Delete API keys
        apiKeyRepository.deleteAllByUserId(userId);

        // 2. Delete projects from DB
        projectRepository.deleteAllByUserId(userId);

        // 3. Delete GCS user folder recursively
        String sanitized = sanitizeEmail(user.getEmail());
        storageService.deleteFolder(sanitized + "/");

        // 4. Delete user document
        userRepository.deleteById(userId);
    }

    /**
     * Strips special characters from email and removes domain suffix.
     * user.name@gmail.com → username
     */
    public static String sanitizeEmail(String email) {
        String localPart = email.contains("@") ? email.split("@")[0] : email;
        return localPart.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
