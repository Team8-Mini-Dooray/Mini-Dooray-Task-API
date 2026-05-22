package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TaskService;
import com.nhnacademy.taskAPI.task.TaskCreateRequest;
import com.nhnacademy.taskAPI.task.TaskDetailDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskMilestoneRequest;
import com.nhnacademy.taskAPI.task.TaskUpdateRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {
    private static final String USER_ID_HEADER = "X-User-Id";

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long tagId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.getTasks(projectId, tagId, userId));
    }

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
            @RequestParam(required = false) Long milestoneId,
            @RequestParam(required = false) String newMilestoneName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newMilestoneStartDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newMilestoneEndDate,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) String newTagName,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(
                        projectId,
                        request,
                        milestoneId,
                        newMilestoneName,
                        newMilestoneStartDate,
                        newMilestoneEndDate,
                        tagIds,
                        newTagName,
                        userId
                ));
    }

    @PutMapping("/{taskId}/edit")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request, userId));
    }

    @DeleteMapping("/{taskId}")
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

}
