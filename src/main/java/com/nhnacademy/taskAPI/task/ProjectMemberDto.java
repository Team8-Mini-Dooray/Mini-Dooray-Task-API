package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record ProjectMemberDto(
        @NotNull
        String userId
) {}
