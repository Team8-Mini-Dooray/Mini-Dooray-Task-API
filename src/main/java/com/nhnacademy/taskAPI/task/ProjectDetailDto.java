package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProjectDetailDto(
        @NotNull
        Long projectId,
        @NotNull
        String name,
        @NotNull
        String status,
        @NotNull
        List<ProjectMemberDto> members,
        @NotNull
        List<TaskDto> tasks,
        @NotNull
        List<MilestoneDto> milestones
) {}
