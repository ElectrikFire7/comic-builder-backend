package com.fullcomicbuilder.backend.service;

import com.fullcomicbuilder.backend.model.Project;
import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fullcomicbuilder.backend.common.GenerationUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static com.fullcomicbuilder.backend.common.Constants.*;

/**
 * The user's decrypted API key is set as the GOOGLE_API_KEY for each request.
 */
@Slf4j
@Service
public class AgentService {

    private final ApiKeyService  apiKeyService;
    private final ProjectService projectService;
    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/";

    public AgentService(ApiKeyService apiKeyService,
                        ProjectService projectService) {
        this.apiKeyService  = apiKeyService;
        this.projectService = projectService;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public byte[] characterGeneration(String prompt,
                                      MultipartFile[] referenceImages,
                                      MultipartFile currentImage,
                                      AuthenticatedUser principal,
                                      String projectId,
                                      String modelName, 
                                      int width, 
                                      int height){

        if(!ValidateProjectOwnership(principal, projectId)){
            throw new RuntimeException("Project Ownership not validated");
        }

        String apikey = fetchAPIKey(principal);

        JsonObject requestBody = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemText = new JsonObject();

        systemText.addProperty("text", CHARACTER_GENERATION_INSTRUCTION);
        systemParts.add(systemText);
        systemInstruction.add("parts", systemParts);
        requestBody.add("systemInstruction", systemInstruction);

        log.debug(String.valueOf(requestBody));

        // Build contents array
        JsonArray contentsArray = new JsonArray();
        JsonObject contentItem = new JsonObject();
        JsonArray partsArray = new JsonArray();

        // Add main task prompt
        JsonObject mainTaskPart = new JsonObject();
        mainTaskPart.addProperty("text", "MAIN TASK: " + prompt + "Image Resolution: " + width + " x " + height);
        partsArray.add(mainTaskPart);


        // Add reference images if they exist
        if (referenceImages != null && referenceImages.length > 0) {
            GenerationUtils.addReferenceImagesToPartsArray(referenceImages, partsArray);
        }

        // Add current image only if current image exist
        if (currentImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(currentImage, partsArray, "Current Image");
        }

        contentItem.add("parts", partsArray);
        contentsArray.add(contentItem);
        requestBody.add("contents", contentsArray);

        // Add generation config
        GenerationUtils.generationConfigBuilder(requestBody);

        String url = apiUrl + modelName + ":generateContent" + "?key=" + apikey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
                
                // Extract image data from response
                if (responseBody.has("candidates") && responseBody.getAsJsonArray("candidates").size() > 0) {
                    JsonObject candidate = responseBody.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                        JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                        if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("inlineData")) {
                            String imageData = parts.get(0).getAsJsonObject()
                                    .getAsJsonObject("inlineData")
                                    .get("data")
                                    .getAsString();
                            return Base64.getDecoder().decode(imageData);
                        }
                    }
                }
                log.error("Invalid response structure from API");
                return new byte[0];
            } else {
                log.error("API request failed with status code: {}", response.statusCode());
                log.error("Response body: {}", response.body());
                throw new RuntimeException("Generation failed with code: " + response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error during character generation: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    public byte[] locationGeneration(String prompt,
                                      MultipartFile styleImage,
                                      MultipartFile templateImage,
                                      MultipartFile currentImage,
                                      AuthenticatedUser principal,
                                      String projectId,
                                      String modelName,
                                      int width,
                                      int height){

        if(!ValidateProjectOwnership(principal, projectId)){
            throw new RuntimeException("Project Ownership not validated");
        }

        String apikey = fetchAPIKey(principal);

        JsonObject requestBody = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemText = new JsonObject();

        systemText.addProperty("text", LOCATION_GENERATION_INSTRUCTIONS);
        systemParts.add(systemText);
        systemInstruction.add("parts", systemParts);
        requestBody.add("systemInstruction", systemInstruction);

        log.debug(String.valueOf(requestBody));

        // Build contents array
        JsonArray contentsArray = new JsonArray();
        JsonObject contentItem = new JsonObject();
        JsonArray partsArray = new JsonArray();

        // Add main task prompt
        JsonObject mainTaskPart = new JsonObject();
        mainTaskPart.addProperty("text", "MAIN TASK: " + prompt + "Image Resolution: " + width + " x " + height);
        partsArray.add(mainTaskPart);

        // Add Style Image only if current image exist
        if (styleImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(styleImage, partsArray, "Style Image");
        }

        // Add Sketch Template only if current image exist
        if (templateImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(templateImage, partsArray, "Sketch Template");
        }

        // Add current image only if current image exist
        if (currentImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(currentImage, partsArray, "Current Image");
        }

        contentItem.add("parts", partsArray);
        contentsArray.add(contentItem);
        requestBody.add("contents", contentsArray);

        // Add generation config
        GenerationUtils.generationConfigBuilder(requestBody);

        String url = apiUrl + modelName + ":generateContent" + "?key=" + apikey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);

                // Extract image data from response
                if (responseBody.has("candidates") && responseBody.getAsJsonArray("candidates").size() > 0) {
                    JsonObject candidate = responseBody.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                        JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                        if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("inlineData")) {
                            String imageData = parts.get(0).getAsJsonObject()
                                    .getAsJsonObject("inlineData")
                                    .get("data")
                                    .getAsString();
                            return Base64.getDecoder().decode(imageData);
                        }
                    }
                }
                log.error("Invalid response structure from API");
                return new byte[0];
            } else {
                log.error("API request failed with status code: {}", response.statusCode());
                log.error("Response body: {}", response.body());
                return new byte[0];
            }
        } catch (Exception e) {
            log.error("Error during character generation: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    public byte[] sceneGeneration(String prompt,
                                  MultipartFile styleImage,
                                  MultipartFile templateImage,
                                  MultipartFile currentImage,
                                  MultipartFile[] characterList,
                                  MultipartFile locationImage,
                                  AuthenticatedUser principal,
                                  String projectId,
                                  String modelName,
                                  int width,
                                  int height){

        if(!ValidateProjectOwnership(principal, projectId)){
            throw new RuntimeException("Project Ownership not validated");
        }

        String apikey = fetchAPIKey(principal);

        JsonObject requestBody = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemText = new JsonObject();

        systemText.addProperty("text", SCENE_GENERATION_INSTRUCTIONS);
        systemParts.add(systemText);
        systemInstruction.add("parts", systemParts);
        requestBody.add("systemInstruction", systemInstruction);

        log.debug(String.valueOf(requestBody));

        // Build contents array
        JsonArray contentsArray = new JsonArray();
        JsonObject contentItem = new JsonObject();
        JsonArray partsArray = new JsonArray();

        // Add main task prompt
        JsonObject mainTaskPart = new JsonObject();
        mainTaskPart.addProperty("text", "MAIN TASK: " + prompt + "Image Resolution: " + width + " x " + height);
        partsArray.add(mainTaskPart);

        // Add Style Image only if current image exist
        if (styleImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(styleImage, partsArray, "Style Image");
        }

        // Add Sketch Template only if current image exist
        if (templateImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(templateImage, partsArray, "Sketch Template");
        }

        // Add current image only if current image exist
        if (currentImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(currentImage, partsArray, "Current Image");
        }

        // Add location image only if current image exist
        if (locationImage != null ) {
            GenerationUtils.addSingleImageToPartsArray(locationImage, partsArray, "Location Image");
        }

        if (characterList != null && characterList.length > 0) {
            GenerationUtils.addCharacterImagesToPartsArray(characterList, partsArray);
        }

        contentItem.add("parts", partsArray);
        contentsArray.add(contentItem);
        requestBody.add("contents", contentsArray);

        // Add generation config
        GenerationUtils.generationConfigBuilder(requestBody);

        String url = apiUrl + modelName + ":generateContent" + "?key=" + apikey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);

                // Extract image data from response
                if (responseBody.has("candidates") && responseBody.getAsJsonArray("candidates").size() > 0) {
                    JsonObject candidate = responseBody.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                        JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                        if (parts.size() > 0 && parts.get(0).getAsJsonObject().has("inlineData")) {
                            String imageData = parts.get(0).getAsJsonObject()
                                    .getAsJsonObject("inlineData")
                                    .get("data")
                                    .getAsString();
                            return Base64.getDecoder().decode(imageData);
                        }
                    }
                }
                log.error("Invalid response structure from API");
                return new byte[0];
            } else {
                log.error("API request failed with status code: {}", response.statusCode());
                log.error("Response body: {}", response.body());
                return new byte[0];
            }
        } catch (Exception e) {
            log.error("Error during character generation: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    public Boolean ValidateProjectOwnership (AuthenticatedUser principal, String projectId) {
        Project project = projectService.getOwnedProject(principal.getUserId(), projectId);

        return project != null;
    }

    public String fetchAPIKey(AuthenticatedUser principal){
        String apiKey = apiKeyService.getDecryptedActiveKey(principal.getUserId());
        log.debug("Retrieved API key for user: {}", apiKey);

        return apiKey;
    }
}
