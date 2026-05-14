package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TaskTagRequest(
        @NotEmpty
        List<@NotNull Long> tagIds
) {}
