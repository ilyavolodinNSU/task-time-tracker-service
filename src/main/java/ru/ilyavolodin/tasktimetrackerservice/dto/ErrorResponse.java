package ru.ilyavolodin.tasktimetrackerservice.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {
    public static ErrorResponse of(int status, String message, String path) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            HttpStatus.valueOf(status).getReasonPhrase(),
            message,
            path
        );
    }
}
