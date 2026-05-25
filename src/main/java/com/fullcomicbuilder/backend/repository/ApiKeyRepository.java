package com.fullcomicbuilder.backend.repository;

import com.fullcomicbuilder.backend.model.ApiKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
    List<ApiKey> findAllByUserId(String userId);
    void deleteAllByUserId(String userId);
    long countByUserId(String userId);
}
