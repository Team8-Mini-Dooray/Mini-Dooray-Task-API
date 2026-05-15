package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TaskTagService;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskTagRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/tasks/{taskId}/tags")
@RequiredArgsConstructor
public class TaskTagController {
    private static final String USER_ID_HEADER = "X-User-Id";

    private final TaskTagService taskTagService;

    @PutMapping
    public ResponseEntity<TaskDto> updateTaskTags(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskTagRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskTagService.updateTaskTags(projectId, taskId, request, userId));
    }
}
