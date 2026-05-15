package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

public record ProjectMemberRequest(
        @NotBlank
        @Size(max = 50)
        String userId
) {}
