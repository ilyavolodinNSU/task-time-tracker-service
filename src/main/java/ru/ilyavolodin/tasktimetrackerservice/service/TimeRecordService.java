package ru.ilyavolodin.tasktimetrackerservice.service;

import java.time.LocalDateTime;
import java.util.List;

import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTimeRecordRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TimeRecordDto;

public interface TimeRecordService {
    public TimeRecordDto createTimeRecord(CreateTimeRecordRequest request);

    public List<TimeRecordDto> getRecordsByEmployeeAndPeriod(
        Long employeeId, 
        LocalDateTime start, 
        LocalDateTime end
    );
}
