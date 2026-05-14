package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;

public record TaskUpdateRequest(
        @NotNull
        String title,
        String content
        @NotBlank String title,
        @NotBlank String content
) {}
