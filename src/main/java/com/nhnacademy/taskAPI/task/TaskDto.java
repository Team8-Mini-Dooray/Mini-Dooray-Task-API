package com.nhnacademy.taskAPI.task;

import java.time.LocalDateTime;

public record TaskDto(
        Long taskId,
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt
) {}
