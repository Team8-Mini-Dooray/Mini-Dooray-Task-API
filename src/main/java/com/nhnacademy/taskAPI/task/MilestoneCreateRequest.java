package com.nhnacademy.taskAPI.task;

import java.time.LocalDate;

public record MilestoneCreateRequest(
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}
