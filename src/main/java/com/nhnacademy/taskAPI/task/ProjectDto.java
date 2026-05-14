package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record ProjectDto(
        @NotNull
        Long projectId,
        @NotNull
        String name,
        @NotNull
        String status
) {}
