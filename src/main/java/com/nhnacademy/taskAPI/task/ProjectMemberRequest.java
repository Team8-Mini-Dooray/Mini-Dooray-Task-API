package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;

public record ProjectMemberRequest(
        @NotNull
        String userId
) {}
