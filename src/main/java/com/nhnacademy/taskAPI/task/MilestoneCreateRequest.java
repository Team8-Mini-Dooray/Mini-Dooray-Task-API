package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MilestoneCreateRequest(
        @NotNull
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}
