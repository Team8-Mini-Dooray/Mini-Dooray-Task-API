package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

public record ProjectCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name
) {}
