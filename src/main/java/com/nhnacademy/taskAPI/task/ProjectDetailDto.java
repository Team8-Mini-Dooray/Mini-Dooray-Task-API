package com.nhnacademy.taskAPI.task;

import com.nhnacademy.taskAPI.entity.Tag;

import java.util.List;

public record ProjectDetailDto(
        Long projectId,
        String name,
        String status,
        String adminId,
        List<ProjectMemberDto> members,
        List<TaskDto> tasks,
        List<MilestoneDto> milestones
) {}