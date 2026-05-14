package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TaskCreateRequest(
        Long taskId,
        @NotNull
        Long projectId,
        @NotNull
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt
) {}
