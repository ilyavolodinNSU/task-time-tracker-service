package ru.ilyavolodin.tasktimetrackerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.ilyavolodin.tasktimetrackerservice.config.TestSecurityConfig;
import ru.ilyavolodin.tasktimetrackerservice.controller.TaskController;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTaskRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TaskDto;
import ru.ilyavolodin.tasktimetrackerservice.dto.UpdateStatusRequest;
import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;
import ru.ilyavolodin.tasktimetrackerservice.exception.ResourceNotFoundException;
import ru.ilyavolodin.tasktimetrackerservice.exception.ValidationConflictException;
import ru.ilyavolodin.tasktimetrackerservice.mapper.TaskMapper;
import ru.ilyavolodin.tasktimetrackerservice.repository.TaskRepository;
import ru.ilyavolodin.tasktimetrackerservice.service.TaskService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController")
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTaskTests {
        @Test
        @DisplayName("должен вернуть 201 Created при успешном создании")
        void shouldReturn201WhenTaskCreated() throws Exception {
            var request = new CreateTaskRequest("Задача", "Описание");
            var response = new TaskDto(1L, "Задача", "Описание", TaskStatus.NEW);
            given(taskService.createTask(request)).willReturn(response);
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Задача"))
                    .andExpect(jsonPath("$.status").value("NEW"));
        }

        @Test
        @DisplayName("должен вернуть 400 при невалидном запросе")
        void shouldReturn400WhenValidationFails() throws Exception {
            var request = new CreateTaskRequest("", "Описание"); // пустой title
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetTaskTests {

        @Test
        @DisplayName("должен вернуть 200 с задачей")
        void shouldReturn200WithTask() throws Exception {
            Long taskId = 1L;
            var taskDto = new TaskDto(taskId, "Найдена", "Описание", TaskStatus.IN_PROGRESS);
            given(taskService.getTaskById(taskId)).willReturn(taskDto);
            mockMvc.perform(get("/api/tasks/{id}", taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Найдена"));
        }

        @Test
        @DisplayName("должен вернуть 404 когда задача не найдена")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            Long taskId = 999L;
            given(taskService.getTaskById(taskId))
                    .willThrow(new ResourceNotFoundException("Task", taskId));
            mockMvc.perform(get("/api/tasks/{id}", taskId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{id}/status")
    class UpdateStatusTests {
        @Test
        @DisplayName("должен вернуть 204 при успешном обновлении")
        void shouldReturn204WhenStatusUpdated() throws Exception {
            Long taskId = 1L;
            var request = new UpdateStatusRequest(TaskStatus.IN_PROGRESS);
            mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("должен вернуть 404 когда задача не найдена")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            Long taskId = 999L;
            var request = new UpdateStatusRequest(TaskStatus.DONE);
            doThrow(new ResourceNotFoundException("Task", taskId))
                    .when(taskService).updateTaskStatus(taskId, request);
            mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("должен вернуть 409 при нарушении бизнес-правила")
        void shouldReturn409WhenBusinessRuleViolated() throws Exception {
            Long taskId = 1L;
            var request = new UpdateStatusRequest(TaskStatus.IN_PROGRESS);
            doThrow(new ValidationConflictException("Cannot change status of completed task"))
                    .when(taskService).updateTaskStatus(taskId, request);
            mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot change status of completed task"));
        }
    }
}
