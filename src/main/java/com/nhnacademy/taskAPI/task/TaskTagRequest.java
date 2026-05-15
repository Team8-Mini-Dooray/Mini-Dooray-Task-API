package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.*;

import java.util.List;

public record TaskTagRequest(
        @NotEmpty
        List<@NotNull Long> tagIds
) {}
