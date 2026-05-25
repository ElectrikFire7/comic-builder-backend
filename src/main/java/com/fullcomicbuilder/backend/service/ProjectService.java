package com.fullcomicbuilder.backend.service;

import com.fullcomicbuilder.backend.model.Project;
import com.fullcomicbuilder.backend.model.User;
import com.fullcomicbuilder.backend.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService       userService;
    private final StorageService    storageService;

    public ProjectService(ProjectRepository projectRepository,
                          UserService userService,
                          StorageService storageService) {
        this.projectRepository = projectRepository;
        this.userService       = userService;
        this.storageService    = storageService;
    }

    public Project createProject(String userId, String projectName) {
        User user = userService.findById(userId);
        String sanitizedEmail = UserService.sanitizeEmail(user.getEmail());
        String folderPath = sanitizedEmail + "/" + projectName;

        Project project = Project.builder()
                .userId(userId)
                .projectName(projectName)
                .storageFolderPath(folderPath)
                .build();

        Project saved = projectRepository.save(project);

        // Create GCS folder structure
        storageService.createProjectFolders(folderPath);

        return saved;
    }

    public List<Project> listProjects(String userId) {
        return projectRepository.findAllByUserId(userId);
    }

    public Project renameProject(String userId, String projectId, String newName) {
        Project project = getOwnedProject(userId, projectId);

        String oldPath = project.getStorageFolderPath();
        User user = userService.findById(userId);
        String sanitizedEmail = UserService.sanitizeEmail(user.getEmail());
        String newPath = sanitizedEmail + "/" + newName;

        // Rename in GCS (copy + delete)
        storageService.renameFolder(oldPath + "/", newPath + "/");

        project.setProjectName(newName);
        project.setStorageFolderPath(newPath);
        return projectRepository.save(project);
    }

    public void deleteProject(String userId, String projectId) {
        Project project = getOwnedProject(userId, projectId);

        // Delete GCS folder recursively
        storageService.deleteFolder(project.getStorageFolderPath() + "/");

        projectRepository.deleteById(projectId);
    }

    public Project getOwnedProject(String userId, String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!project.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return project;
    }
}
