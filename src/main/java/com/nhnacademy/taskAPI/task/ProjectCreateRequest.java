package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record ProjectCreateRequest(
        @NotNull
        String name
) {}
