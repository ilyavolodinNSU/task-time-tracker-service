package ru.ilyavolodin.tasktimetrackerservice.entity;

import lombok.Data;

@Data
public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
}
