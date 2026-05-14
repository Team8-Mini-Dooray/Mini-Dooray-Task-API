package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record TaskUpdateRequest(
        @NotNull
        String title,
        String content
) {}
