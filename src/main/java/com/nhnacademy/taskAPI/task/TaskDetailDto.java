package com.nhnacademy.taskAPI.task;

import java.time.LocalDateTime;
import java.util.List;

public record TaskDetailDto(
        Long taskId,
        String title,
        String content,
        String writerId,
        LocalDateTime createdAt,
        List<CommentDto> comments
) {
}