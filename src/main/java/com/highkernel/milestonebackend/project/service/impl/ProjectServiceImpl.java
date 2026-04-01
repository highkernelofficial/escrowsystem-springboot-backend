package com.highkernel.milestonebackend.project.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.highkernel.milestonebackend.exception.BadRequestException;
import com.highkernel.milestonebackend.project.dto.ProjectCreateRequest;
import com.highkernel.milestonebackend.project.dto.ProjectResponse;
import com.highkernel.milestonebackend.project.dto.ProjectUpdateRequest;
import com.highkernel.milestonebackend.project.entity.Project;
import com.highkernel.milestonebackend.project.repository.ProjectRepository;
import com.highkernel.milestonebackend.project.service.ProjectService;
import com.highkernel.milestonebackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ProjectResponse createProject(String authenticatedUserId, ProjectCreateRequest request) {
        UUID clientId = parseUuid(authenticatedUserId, "Invalid authenticated user id");

        userRepository.findById(clientId)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));

        Project project = Project.builder()
                .clientId(clientId)
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(toJson(request.getTechStack()))
                .expectedOutcome(request.getExpectedOutcome())
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "OPEN")
                .appId(request.getAppId())
                .totalAmount(request.getTotalAmount())
                .build();

        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = getProjectOrThrow(projectId);
        return mapToResponse(project);
    }

    @Override
    public List<ProjectResponse> getProjectsByClientId(UUID clientId) {
        userRepository.findById(clientId)
                .orElseThrow(() -> new BadRequestException("Client not found"));

        return projectRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProjectResponse updateProject(String authenticatedUserId, UUID projectId, ProjectUpdateRequest request) {
        UUID authUserId = parseUuid(authenticatedUserId, "Invalid authenticated user id");

        Project project = getProjectOrThrow(projectId);

        if (!project.getClientId().equals(authUserId)) {
            throw new BadRequestException("You can update only your own project");
        }

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getTechStack() != null) {
            project.setTechStack(toJson(request.getTechStack()));
        }
        if (request.getExpectedOutcome() != null) {
            project.setExpectedOutcome(request.getExpectedOutcome());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getAppId() != null) {
            project.setAppId(request.getAppId());
        }
        if (request.getTotalAmount() != null) {
            project.setTotalAmount(request.getTotalAmount());
        }

        Project updatedProject = projectRepository.save(project);
        return mapToResponse(updatedProject);
    }

    @Override
    public void deleteProject(String authenticatedUserId, UUID projectId) {
        UUID authUserId = parseUuid(authenticatedUserId, "Invalid authenticated user id");

        Project project = getProjectOrThrow(projectId);

        if (!project.getClientId().equals(authUserId)) {
            throw new BadRequestException("You can delete only your own project");
        }

        projectRepository.delete(project);
    }

    private Project getProjectOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("Project not found"));
    }

    private UUID parseUuid(String value, String errorMessage) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new BadRequestException(errorMessage);
        }
    }

    private String toJson(List<String> techStack) {
        try {
            return objectMapper.writeValueAsString(techStack);
        } catch (Exception ex) {
            throw new BadRequestException("Failed to serialize tech stack");
        }
    }

    private List<String> fromJson(String techStackJson) {
        if (techStackJson == null || techStackJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(techStackJson, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            throw new BadRequestException("Failed to parse tech stack");
        }
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .clientId(project.getClientId())
                .title(project.getTitle())
                .description(project.getDescription())
                .techStack(fromJson(project.getTechStack()))
                .expectedOutcome(project.getExpectedOutcome())
                .status(project.getStatus())
                .appId(project.getAppId())
                .totalAmount(project.getTotalAmount())
                .createdAt(project.getCreatedAt())
                .build();
    }
}