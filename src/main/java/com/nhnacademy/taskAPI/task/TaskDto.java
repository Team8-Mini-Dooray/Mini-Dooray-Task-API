package com.nhnacademy.taskAPI.task;

import java.time.LocalDateTime;

public record TaskDto(
        Long taskId,
        Long milestoneId,
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt
) {}
