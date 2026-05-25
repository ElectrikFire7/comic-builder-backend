package com.fullcomicbuilder.backend.controller;

import com.fullcomicbuilder.backend.dto.request.CreateProjectRequest;
import com.fullcomicbuilder.backend.dto.response.ProjectResponse;
import com.fullcomicbuilder.backend.model.Project;
import com.fullcomicbuilder.backend.security.AuthenticatedUser;
import com.fullcomicbuilder.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateProjectRequest req) {

        Project project = projectService.createProject(principal.getUserId(), req.getProjectName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(
            @AuthenticationPrincipal AuthenticatedUser principal) {

        List<ProjectResponse> projects = projectService.listProjects(principal.getUserId())
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> renameProject(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId,
            @Valid @RequestBody CreateProjectRequest req) {

        Project project = projectService.renameProject(principal.getUserId(), projectId, req.getProjectName());
        return ResponseEntity.ok(toResponse(project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String projectId) {

        projectService.deleteProject(principal.getUserId(), projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .projectName(p.getProjectName())
                .storageFolderPath(p.getStorageFolderPath())
                .build();
    }
}
