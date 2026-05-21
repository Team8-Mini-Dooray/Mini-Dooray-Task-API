package com.nhnacademy.taskAPI.exception;

public record ErrorResponse(
        int status,
        String message,
        String path
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.getStatusValue(),
                errorCode.getMessage(),
                path
        );
    }
}