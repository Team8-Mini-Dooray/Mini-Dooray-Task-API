package com.nhnacademy.taskAPI.task;

import java.time.LocalDate;
import java.util.List;

public record MilestoneDetailDto(
        Long milestoneId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        List<TaskDto> tasks
) {}