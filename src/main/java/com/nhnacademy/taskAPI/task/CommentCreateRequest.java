package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank
        String content
) {}
