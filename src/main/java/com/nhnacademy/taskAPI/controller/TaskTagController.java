package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TaskTagService;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskTagRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/tasks/{taskId}/tags")
@RequiredArgsConstructor
public class TaskTagController {
    private static final String USER_ID_HEADER = "X-User-Id";

    private final TaskTagService taskTagService;

    @PostMapping
    public ResponseEntity<TaskDto> updateTaskTags(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskTagRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskTagService.updateTaskTags(projectId, taskId, request, userId));
    }
}
