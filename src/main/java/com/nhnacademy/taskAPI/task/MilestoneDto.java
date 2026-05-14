package com.nhnacademy.taskAPI.task;

import java.time.LocalDate;

public record MilestoneDto(
        Long milestoneId,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}
