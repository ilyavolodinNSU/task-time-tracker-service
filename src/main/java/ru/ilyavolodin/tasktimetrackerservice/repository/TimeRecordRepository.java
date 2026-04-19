package ru.ilyavolodin.tasktimetrackerservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;

public interface TimeRecordRepository {
    public void save(TimeRecord record);

    public List<TimeRecord> findByEmployeeAndPeriod(
        Long employeeId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
    );
}
