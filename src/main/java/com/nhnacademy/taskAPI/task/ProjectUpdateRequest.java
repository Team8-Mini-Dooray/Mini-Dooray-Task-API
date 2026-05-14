package com.nhnacademy.taskAPI.task;

public record ProjectUpdateRequest(
        String name,
        String status // ACTIVE, DORMANT, CLOSED
) {}
