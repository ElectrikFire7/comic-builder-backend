package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.request.AddApiKeyRequest;
import com.fullcomicbuilder.backend.dto.response.ApiKeyResponse;
import com.fullcomicbuilder.backend.model.ApiKey;
import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.fullcomicbuilder.backend.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> addKey(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody AddApiKeyRequest req) {

        ApiKey key = apiKeyService.addKey(
                principal.getUserId(), req.getApiKeyName(), req.getApiKeyValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(key));
    }

    /** Returns key names only — value is never exposed. */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listKeys(
            @AuthenticationPrincipal AuthenticatedUser principal) {

        List<ApiKeyResponse> keys = apiKeyService.listKeys(principal.getUserId())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(keys);
    }

    @PutMapping("/{keyId}/set-active")
    public ResponseEntity<Map<String, String>> setActive(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String keyId) {

        apiKeyService.setActive(principal.getUserId(), keyId);
        return ResponseEntity.ok(Map.of("message", "Active API key updated"));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Map<String, String>> deleteKey(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String keyId) {

        apiKeyService.deleteKey(principal.getUserId(), keyId);
        return ResponseEntity.ok(Map.of("message", "API key deleted"));
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return ApiKeyResponse.builder()
                .id(key.getId())
                .apiKeyName(key.getApiKeyName())
                .build();
    }
}
