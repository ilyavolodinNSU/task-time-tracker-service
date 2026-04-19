package ru.ilyavolodin.tasktimetrackerservice.dto;

import java.time.LocalDateTime;

public record TimeRecordDto (
    Long id,
    Long employeeId,
    Long taskId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String workDescription
) {}
