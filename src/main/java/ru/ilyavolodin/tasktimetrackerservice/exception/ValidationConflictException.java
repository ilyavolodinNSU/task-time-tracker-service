package ru.ilyavolodin.tasktimetrackerservice.exception;

import org.springframework.http.HttpStatus;

public class ValidationConflictException extends BusinessException {
    public ValidationConflictException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}
