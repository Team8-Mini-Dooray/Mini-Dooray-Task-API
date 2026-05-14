package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record MilestoneCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}
