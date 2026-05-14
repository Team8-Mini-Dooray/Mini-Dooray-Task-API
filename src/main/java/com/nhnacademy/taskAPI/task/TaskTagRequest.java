package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TaskTagRequest(
        @NotNull
        @Size(min = 1)
        List<Long> tagIds
        @NotEmpty
        List<@NotNull Long> tagIds
) {}
