package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.CommentService;
import com.nhnacademy.taskAPI.task.CommentCreateRequest;
import com.nhnacademy.taskAPI.task.CommentDto;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private static final String USER_ID_HEADER = "X-User-Id";

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(commentService.getComments(projectId, taskId, userId));
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.createComment(projectId, taskId, request, userId));
    }

    @PostMapping("/{commentId}/edit")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        return ResponseEntity.ok(commentService.updateComment(projectId, taskId, commentId, request, userId));
    }

    @PostMapping("/{commentId}/delete")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @RequestHeader(USER_ID_HEADER) String userId
    ) {
        commentService.deleteComment(projectId, taskId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
