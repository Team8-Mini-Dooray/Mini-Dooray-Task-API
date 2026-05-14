package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

public record TagCreateRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {}
