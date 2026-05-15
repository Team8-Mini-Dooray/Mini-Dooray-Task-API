package com.nhnacademy.taskAPI.task;

import java.time.LocalDateTime;

public record CommentDto(
        Long commentId,
        String writerId,
        String content,
        LocalDateTime createdAt
) {
}
