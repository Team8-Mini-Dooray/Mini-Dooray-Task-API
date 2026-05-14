package com.nhnacademy.taskAPI.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TaskTagRequest(
        @NotNull
        @Size(min = 1)
        List<Long> tagIds
) {}
