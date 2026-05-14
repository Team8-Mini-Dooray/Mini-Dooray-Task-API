package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull
        String content
) {}
