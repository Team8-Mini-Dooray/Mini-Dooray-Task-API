package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TaskDto(
        @NotNull
        Long taskId,
        @NotNull
        String title,
        String content,
        @NotNull
        String writerId,
        @NotNull
        LocalDateTime createdAt
) {}
