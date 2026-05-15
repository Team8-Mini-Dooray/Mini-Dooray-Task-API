package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

public record TaskUpdateRequest(
        @NotBlank
        @Size(max = 200)
        String title,
        String content
) {}
