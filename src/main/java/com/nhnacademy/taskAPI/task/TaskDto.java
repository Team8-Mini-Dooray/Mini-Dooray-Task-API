package com.nhnacademy.taskAPI.task;

import java.time.LocalDateTime;
import java.util.List;

public record TaskDto(
        Long taskId,
        Long milestoneId,
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt,
        List<TagDto> tags
) {}
