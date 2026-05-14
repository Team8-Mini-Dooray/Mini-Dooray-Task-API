package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MilestoneDto(
        @NotNull
        Long milestoneId,
        @NotNull
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}
