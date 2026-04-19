package ru.ilyavolodin.tasktimetrackerservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;
import ru.ilyavolodin.tasktimetrackerservice.mybatis.MyBatisTimeRecordMapper;

@Repository
@RequiredArgsConstructor
public class MyBatisTimeRecordRepositoryImpl implements TimeRecordRepository {
    private final MyBatisTimeRecordMapper myBatisTimeRecordMapper;

    public void save(TimeRecord record) {
        myBatisTimeRecordMapper.insert(record);
    }

    public List<TimeRecord> findByEmployeeAndPeriod(
        Long employeeId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
    ) {
        return myBatisTimeRecordMapper.select(employeeId, periodStart, periodEnd);
    }
}
