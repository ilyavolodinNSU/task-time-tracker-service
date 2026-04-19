package ru.ilyavolodin.tasktimetrackerservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;
import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class TimeRecordRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("tasktimetracker_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    @Autowired
    private TaskRepository taskRepository;

    private Long taskId;

    @BeforeEach
    void setUp() {
        Task task = new Task();
        task.setTitle("Родительская задача для записей времени");
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);
        taskId = task.getId();
    }

    @Test
    void shouldSaveAndFindRecordsByEmployeeAndPeriod() {
        TimeRecord record = new TimeRecord();
        record.setEmployeeId(1L);
        record.setTaskId(taskId);
        record.setStartTime(LocalDateTime.of(2026, 4, 19, 10, 0));
        record.setEndTime(LocalDateTime.of(2026, 4, 19, 12, 0));
        record.setWorkDescription("Разработка DAO");
        timeRecordRepository.save(record);
        LocalDateTime start = LocalDateTime.of(2026, 4, 19, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 19, 23, 59);
        List<TimeRecord> found = timeRecordRepository.findByEmployeeAndPeriod(1L, start, end);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getWorkDescription()).isEqualTo("Разработка DAO");
        assertThat(found.get(0).getTaskId()).isEqualTo(taskId);
    }

    @Test
    void shouldReturnEmptyListForNonOverlappingPeriod() {
        TimeRecord record = new TimeRecord();
        record.setEmployeeId(2L);
        record.setTaskId(taskId);
        record.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        record.setEndTime(LocalDateTime.of(2026, 5, 1, 12, 0));
        timeRecordRepository.save(record);
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);
        List<TimeRecord> found = timeRecordRepository.findByEmployeeAndPeriod(2L, start, end);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnMultipleRecordsForSameEmployee() {
        TimeRecord r1 = new TimeRecord();
        r1.setEmployeeId(3L);
        r1.setTaskId(taskId);
        r1.setStartTime(LocalDateTime.of(2026, 4, 19, 9, 0));
        r1.setEndTime(LocalDateTime.of(2026, 4, 19, 10, 0));
        timeRecordRepository.save(r1);
        TimeRecord r2 = new TimeRecord();
        r2.setEmployeeId(3L);
        r2.setTaskId(taskId);
        r2.setStartTime(LocalDateTime.of(2026, 4, 19, 11, 0));
        r2.setEndTime(LocalDateTime.of(2026, 4, 19, 13, 0));
        timeRecordRepository.save(r2);
        LocalDateTime start = LocalDateTime.of(2026, 4, 19, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 19, 23, 59);
        List<TimeRecord> found = timeRecordRepository.findByEmployeeAndPeriod(3L, start, end);
        assertThat(found).hasSize(2);
    }
}
