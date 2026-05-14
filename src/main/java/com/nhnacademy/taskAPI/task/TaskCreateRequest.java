package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record TaskCreateRequest(
        Long taskId,
        @NotNull
        Long projectId,
        @NotBlank
        @Size(max = 200)
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt
) {}
