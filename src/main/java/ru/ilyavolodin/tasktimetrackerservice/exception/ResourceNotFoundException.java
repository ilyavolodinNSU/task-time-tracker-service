package ru.ilyavolodin.tasktimetrackerservice.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super("%s not found with id: %d".formatted(resourceName, id), HttpStatus.NOT_FOUND.value());
    }
    
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
