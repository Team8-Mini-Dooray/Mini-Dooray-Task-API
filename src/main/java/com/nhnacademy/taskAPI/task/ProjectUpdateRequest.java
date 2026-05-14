package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record ProjectUpdateRequest(
        @NotNull
        String name,
        @NotNull
        String status // ACTIVE, DORMANT, TERMINATED
) {}
