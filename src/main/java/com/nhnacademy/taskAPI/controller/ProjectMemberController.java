package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.ProjectMemberService;
import com.nhnacademy.taskAPI.task.ProjectMemberDto;
import com.nhnacademy.taskAPI.task.ProjectMemberRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<ProjectMemberDto>> getMembers(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(projectMemberService.getMembers(projectId, userId));
    }
    @PostMapping
    public ResponseEntity<ProjectMemberDto> addMember(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProjectMemberRequest request
    ) {
        ProjectMemberDto response = projectMemberService.addMember(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/{userId}/delete")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String requestId,
            @PathVariable String userId
    ) {
        projectMemberService.removeMember(projectId, requestId, userId);
        return ResponseEntity.noContent().build();
    }
}
