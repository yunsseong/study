package com.example.blog.global.error;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), List.of(), LocalDateTime.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), fieldErrors, LocalDateTime.now());
    }

    public record FieldError(String field, String value, String reason) {
    }
}
