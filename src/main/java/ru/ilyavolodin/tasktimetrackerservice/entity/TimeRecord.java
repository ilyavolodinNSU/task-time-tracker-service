package ru.ilyavolodin.tasktimetrackerservice.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TimeRecord {
    private Long id;
    private Long employeeId;
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String workDescription;
}
