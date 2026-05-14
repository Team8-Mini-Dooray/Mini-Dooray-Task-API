package com.nhnacademy.taskAPI.task;

import java.time.LocalDateTime;

public record TaskCreateRequest(
        Long taskId,
        Long projectId,
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt
) {}
