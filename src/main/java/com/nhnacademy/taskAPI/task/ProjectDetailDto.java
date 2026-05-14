package com.nhnacademy.taskAPI.task;

import java.util.List;

public record ProjectDetailDto(
        Long projectId,
        String name,
        String status,
        List<ProjectMemberDto> members,
        List<TaskDto> tasks,
        List<MilestoneDto> milestones
) {}

