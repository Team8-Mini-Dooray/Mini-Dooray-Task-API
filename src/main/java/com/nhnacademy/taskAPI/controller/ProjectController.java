package com.nhnacademy.taskAPI.controller;


import com.nhnacademy.taskAPI.service.ProjectService;
import com.nhnacademy.taskAPI.task.ProjectCreateRequest;
import com.nhnacademy.taskAPI.task.ProjectDetailDto;
import com.nhnacademy.taskAPI.task.ProjectDto;
import com.nhnacademy.taskAPI.task.ProjectUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(projectService.getProjects(userId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailDto> getProjectDetail(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(projectService.getProjectDetail(projectId, userId));
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        ProjectDto response = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/{projectId}/edit")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(projectId, userId, request));
    }
    @PostMapping("/{projectId}/close")
    public ResponseEntity<ProjectDto> closeProject(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(projectService.closeProject(projectId, userId));
    }
}

