package ru.ilyavolodin.tasktimetrackerservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTimeRecordRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TimeRecordDto;
import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;
import ru.ilyavolodin.tasktimetrackerservice.exception.ResourceNotFoundException;
import ru.ilyavolodin.tasktimetrackerservice.exception.ValidationConflictException;
import ru.ilyavolodin.tasktimetrackerservice.mapper.TimeRecordMapper;
import ru.ilyavolodin.tasktimetrackerservice.repository.TaskRepository;
import ru.ilyavolodin.tasktimetrackerservice.repository.TimeRecordRepository;

@Service
@RequiredArgsConstructor
public class TimeRecordServiceImpl implements TimeRecordService {
    private final TimeRecordRepository timeRecordRepository;
    private final TaskRepository taskRepository;
    private final TimeRecordMapper timeRecordMapper;

    @Override
    @Transactional
    public TimeRecordDto createTimeRecord(CreateTimeRecordRequest request) {
        if (request.endTime().isBefore(request.startTime()) || 
            request.endTime().isEqual(request.startTime()))
                throw new ValidationConflictException("End time must be after start time");
        
        taskRepository.findById(request.taskId())
            .orElseThrow(() -> new ResourceNotFoundException("Task", request.taskId()));

        TimeRecord entity = timeRecordMapper.toEntity(request);
        timeRecordRepository.save(entity);

        return timeRecordMapper.toDto(entity);
    }

    @Override
    public List<TimeRecordDto> getRecordsByEmployeeAndPeriod(
        Long employeeId, 
        LocalDateTime start, 
        LocalDateTime end
    ) {
        if (end.isBefore(start)) 
            throw new ValidationConflictException("End of period cannot be before start of period");

        return timeRecordRepository
                    .findByEmployeeAndPeriod(employeeId, start, end).stream()
                    .map(timeRecordMapper::toDto)
                    .toList();
    }
}
