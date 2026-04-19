package ru.ilyavolodin.tasktimetrackerservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
    @NotBlank(message = "Название задачи обязательно")
    String title,
    String description
) {}
