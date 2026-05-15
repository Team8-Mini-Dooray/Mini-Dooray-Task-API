package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TaskService;
import com.nhnacademy.taskAPI.task.CommentCreateRequest;
import com.nhnacademy.taskAPI.task.CommentDto;
import com.nhnacademy.taskAPI.task.TaskCreateRequest;
import com.nhnacademy.taskAPI.task.TaskDetailDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskMilestoneRequest;
import com.nhnacademy.taskAPI.task.TaskTagRequest;
import com.nhnacademy.taskAPI.task.TaskUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {
    private static final String USER_ID_HEADER = "X-User-Id";

    private final TaskService taskService;

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDetailDto> getTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.getTask(projectId, taskId, userId));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskCreateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, request, userId));
    }

    @PostMapping("/{taskId}/edit")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request, userId));
    }

    @PostMapping("/{taskId}/delete")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        taskService.deleteTask(projectId, taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/milestones")
    public ResponseEntity<TaskDto> updateTaskMilestone(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskMilestoneRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.updateTaskMilestone(projectId, taskId, request, userId));
    }

    @PostMapping("/{taskId}/tags")
    public ResponseEntity<TaskDto> updateTaskTags(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskTagRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.updateTaskTags(projectId, taskId, request, userId));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createComment(projectId, taskId, request, userId));
    }

    @PostMapping("/{taskId}/comments/{commentId}/edit")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.updateComment(projectId, taskId, commentId, request, userId));
    }

    @PostMapping("/{taskId}/comments/{commentId}/delete")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        taskService.deleteComment(projectId, taskId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
