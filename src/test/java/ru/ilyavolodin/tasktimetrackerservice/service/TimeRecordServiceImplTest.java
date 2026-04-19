package ru.ilyavolodin.tasktimetrackerservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTimeRecordRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TimeRecordDto;
import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;
import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;
import ru.ilyavolodin.tasktimetrackerservice.exception.ResourceNotFoundException;
import ru.ilyavolodin.tasktimetrackerservice.exception.ValidationConflictException;
import ru.ilyavolodin.tasktimetrackerservice.mapper.TimeRecordMapper;
import ru.ilyavolodin.tasktimetrackerservice.repository.TaskRepository;
import ru.ilyavolodin.tasktimetrackerservice.repository.TimeRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeRecordServiceImpl")
class TimeRecordServiceImplTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimeRecordMapper timeRecordMapper;

    @InjectMocks
    private TimeRecordServiceImpl timeRecordService;

    @Nested
    @DisplayName("createTimeRecord")
    class CreateTimeRecordTests {

        @Test
        @DisplayName("должен создать запись о времени")
        void shouldCreateTimeRecord() {
            var request = new CreateTimeRecordRequest(
                    1L, 1L,
                    LocalDateTime.of(2026, 4, 19, 9, 0),
                    LocalDateTime.of(2026, 4, 19, 10, 0),
                    "Работа"
            );
            var task = new Task();
            task.setId(1L);
            var entity = new TimeRecord();
            entity.setId(1L);
            var dto = new TimeRecordDto(1L, 1L, 1L, request.startTime(), request.endTime(), "Работа");
            given(taskRepository.findById(request.taskId())).willReturn(Optional.of(task));
            given(timeRecordMapper.toEntity(request)).willReturn(entity);
            given(timeRecordMapper.toDto(entity)).willReturn(dto);
            var result = timeRecordService.createTimeRecord(request);
            assertThat(result).isEqualTo(dto);
            verify(timeRecordRepository).save(entity);
        }

        @Test
        @DisplayName("должен выбросить исключение если endTime раньше startTime")
        void shouldThrowWhenEndTimeBeforeStartTime() {
            var request = new CreateTimeRecordRequest(
                    1L, 1L,
                    LocalDateTime.of(2026, 4, 19, 10, 0),
                    LocalDateTime.of(2026, 4, 19, 9, 0),
                    "Работа"
            );
            assertThatThrownBy(() -> timeRecordService.createTimeRecord(request))
                    .isInstanceOf(ValidationConflictException.class)
                    .hasMessage("End time must be after start time");
            verify(taskRepository, never()).findById(any());
            verify(timeRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("должен выбросить исключение если endTime равен startTime")
        void shouldThrowWhenEndTimeEqualsStartTime() {
            var sameTime = LocalDateTime.of(2026, 4, 19, 10, 0);
            var request = new CreateTimeRecordRequest(1L, 1L, sameTime, sameTime, "Работа");
            assertThatThrownBy(() -> timeRecordService.createTimeRecord(request))
                    .isInstanceOf(ValidationConflictException.class)
                    .hasMessage("End time must be after start time");
            verify(taskRepository, never()).findById(any());
        }

        @Test
        @DisplayName("должен выбросить исключение если задача не найдена")
        void shouldThrowWhenTaskNotFound() {
            var request = new CreateTimeRecordRequest(
                    1L, 999L,
                    LocalDateTime.of(2026, 4, 19, 9, 0),
                    LocalDateTime.of(2026, 4, 19, 10, 0),
                    "Работа"
            );
            given(taskRepository.findById(request.taskId())).willReturn(Optional.empty());
            assertThatThrownBy(() -> timeRecordService.createTimeRecord(request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(timeRecordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getRecordsByEmployeeAndPeriod")
    class GetRecordsTests {
        @Test
        @DisplayName("должен вернуть список записей за период")
        void shouldReturnRecordsForPeriod() {
            Long employeeId = 1L;
            var start = LocalDateTime.of(2026, 4, 19, 0, 0);
            var end = LocalDateTime.of(2026, 4, 19, 23, 59);
            var entity = new TimeRecord();
            entity.setId(1L);
            entity.setEmployeeId(employeeId);
            var dto = new TimeRecordDto(1L, employeeId, 1L, start, end, "Работа");
            given(timeRecordRepository.findByEmployeeAndPeriod(employeeId, start, end))
                    .willReturn(List.of(entity));
            given(timeRecordMapper.toDto(entity)).willReturn(dto);
            var result = timeRecordService.getRecordsByEmployeeAndPeriod(employeeId, start, end);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(dto);
        }

        @Test
        @DisplayName("должен вернуть пустой список если записей нет")
        void shouldReturnEmptyListWhenNoRecords() {
            Long employeeId = 1L;
            var start = LocalDateTime.of(2026, 4, 19, 0, 0);
            var end = LocalDateTime.of(2026, 4, 19, 23, 59);
            given(timeRecordRepository.findByEmployeeAndPeriod(employeeId, start, end))
                    .willReturn(List.of());
            var result = timeRecordService.getRecordsByEmployeeAndPeriod(employeeId, start, end);
            assertThat(result).isEmpty();
            verify(timeRecordRepository).findByEmployeeAndPeriod(employeeId, start, end);
        }

        @Test
        @DisplayName("должен выбросить исключение если end периода раньше start")
        void shouldThrowWhenPeriodEndBeforeStart() {
            var start = LocalDateTime.of(2026, 4, 20, 0, 0);
            var end = LocalDateTime.of(2026, 4, 19, 0, 0);
            assertThatThrownBy(() -> timeRecordService.getRecordsByEmployeeAndPeriod(1L, start, end))
                    .isInstanceOf(ValidationConflictException.class)
                    .hasMessage("End of period cannot be before start of period");

            verify(timeRecordRepository, never()).findByEmployeeAndPeriod(any(), any(), any());
        }
    }
}