package ru.ilyavolodin.tasktimetrackerservice.dto;

import jakarta.validation.constraints.NotNull;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;

public record UpdateStatusRequest(
    @NotNull(message = "Статус не может быть null")
    TaskStatus status
) {}
