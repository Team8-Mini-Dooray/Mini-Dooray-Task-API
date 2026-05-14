package com.nhnacademy.taskAPI.exception;

public record ErrorResponse(
        int status,
        String code,
        String message,
        String path
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.getStatusValue(),
                errorCode.getCode(),
                errorCode.getMessage(),
                path
        );
    }
}