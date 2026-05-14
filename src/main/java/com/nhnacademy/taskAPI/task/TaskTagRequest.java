package com.nhnacademy.taskAPI.task;

import java.util.List;

public record TaskTagRequest(
        List<Long> tagIds
) {}
