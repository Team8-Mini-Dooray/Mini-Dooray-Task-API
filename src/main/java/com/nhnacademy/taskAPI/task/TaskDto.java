package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskDto(
        @NotNull Long taskId,
        @NotBlank String title,
        String content,
        String writerId,
        LocalDateTime createdAt
) {}
