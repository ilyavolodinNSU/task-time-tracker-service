package ru.ilyavolodin.tasktimetrackerservice.dto;

import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;

public record TaskDto(
    Long id,
    String title,
    String description,
    TaskStatus status
) {}
