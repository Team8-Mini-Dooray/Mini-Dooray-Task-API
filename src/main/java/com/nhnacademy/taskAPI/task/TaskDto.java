package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskDto(
        @NotNull
        Long taskId,
        @NotNull
        String title,
        @NotNull Long taskId,
        @NotBlank String title,
        String content,
        @NotNull
        String writerId,
        @NotNull
        LocalDateTime createdAt
) {}
