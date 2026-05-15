package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TagService;
import com.nhnacademy.taskAPI.task.TagCreateRequest;
import com.nhnacademy.taskAPI.task.TagDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getTags(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(tagService.getTags(projectId, userId));
    }

    @PostMapping
    public ResponseEntity<TagDto> createTag(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TagCreateRequest request
    ) {
        TagDto response = tagService.createTag(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{tagId}/edit")
    public ResponseEntity<TagDto> updateTag(
            @PathVariable Long projectId,
            @PathVariable Long tagId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TagCreateRequest request
    ) {
        return ResponseEntity.ok(
                tagService.updateTag(projectId, tagId, userId, request)
        );
    }

    @PostMapping("/{tagId}/delete")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long projectId,
            @PathVariable Long tagId,
            @RequestHeader("X-User-Id") String userId
    ) {
        tagService.deleteTag(projectId, tagId, userId);
        return ResponseEntity.noContent().build();
    }
}