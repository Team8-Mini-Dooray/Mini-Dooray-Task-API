package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotBlank;

public record TaskUpdateRequest(
        @NotBlank String title,
        @NotBlank String content
) {}
