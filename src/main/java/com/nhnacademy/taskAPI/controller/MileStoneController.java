package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.MilestoneService;
import com.nhnacademy.taskAPI.task.MilestoneCreateRequest;
import com.nhnacademy.taskAPI.task.MilestoneDetailDto;
import com.nhnacademy.taskAPI.task.MilestoneDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/milestones")
@RequiredArgsConstructor
public class MileStoneController {
    private final MilestoneService milestoneService;

    @GetMapping
    public ResponseEntity<List<MilestoneDto>> getMilestones(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(milestoneService.getMilestones(projectId, userId));
    }
    @GetMapping("/{milestoneId}")
    public ResponseEntity<MilestoneDetailDto> getMilestoneDetail(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(
                milestoneService.getMilestoneDetail(projectId, milestoneId, userId)
        );
    }
    @PostMapping
    public ResponseEntity<MilestoneDto> createMilestone(
            @PathVariable Long projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody MilestoneCreateRequest request
    ) {
        MilestoneDto response = milestoneService.createMilestone(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{milestoneId}/edit")
    public ResponseEntity<MilestoneDto> updateMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody MilestoneCreateRequest request
    ) {
        return ResponseEntity.ok(
                milestoneService.updateMilestone(projectId, milestoneId, userId, request)
        );
    }
    @PostMapping("/{milestoneId}/delete")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @RequestHeader("X-User-Id") String userId
    ) {
        milestoneService.deleteMilestone(projectId, milestoneId, userId);
        return ResponseEntity.noContent().build();
    }
}
