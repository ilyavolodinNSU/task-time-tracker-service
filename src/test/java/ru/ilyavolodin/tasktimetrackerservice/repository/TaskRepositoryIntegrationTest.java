package ru.ilyavolodin.tasktimetrackerservice.repository;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class TaskRepositoryIntegrationTest {
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
    private TaskRepository taskRepository;

    @Test
    void shouldSaveAndFindTask() {
        Task task = new Task();
        task.setTitle("Интеграционный тест");
        task.setDescription("Проверка сохранения и поиска");
        task.setStatus(TaskStatus.NEW);
        taskRepository.save(task);
        Optional<Task> found = taskRepository.findById(task.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Интеграционный тест");
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void shouldUpdateTaskStatus() {
        Task task = new Task();
        task.setTitle("Задача для обновления");
        task.setStatus(TaskStatus.NEW);
        taskRepository.save(task);
        taskRepository.setStatusById(task.getId(), TaskStatus.IN_PROGRESS);
        Optional<Task> updated = taskRepository.findById(task.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldReturnEmptyWhenTaskNotFound() {
        Optional<Task> result = taskRepository.findById(99999L);
        assertThat(result).isEmpty();
    }
}
