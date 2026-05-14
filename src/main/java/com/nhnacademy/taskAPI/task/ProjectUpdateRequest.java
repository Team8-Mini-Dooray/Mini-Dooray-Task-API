package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

public record ProjectUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Pattern(regexp = "ACTIVE|DORMANT|TERMINATED")
        String status // ACTIVE, DORMANT, TERMINATED
) {}
