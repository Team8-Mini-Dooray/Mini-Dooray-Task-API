package com.nhnacademy.taskAPI.task;

public record TaskUpdateRequest(
        String title,
        String content
) {}
