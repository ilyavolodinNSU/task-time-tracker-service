package ru.ilyavolodin.tasktimetrackerservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTaskRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TaskDto;
import ru.ilyavolodin.tasktimetrackerservice.dto.UpdateStatusRequest;
import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;
import ru.ilyavolodin.tasktimetrackerservice.exception.ResourceNotFoundException;
import ru.ilyavolodin.tasktimetrackerservice.exception.ValidationConflictException;
import ru.ilyavolodin.tasktimetrackerservice.mapper.TaskMapper;
import ru.ilyavolodin.tasktimetrackerservice.repository.TaskRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Nested
    @DisplayName("createTask")
    class CreateTaskTests {

        @Test
        @DisplayName("должен создать задачу со статусом NEW")
        void shouldCreateTaskWithNewStatus() {
            var request = new CreateTaskRequest("Тестовая задача", "Описание");
            var taskEntity = new Task();
            taskEntity.setTitle(request.title());
            var taskDto = new TaskDto(1L, request.title(), request.description(), TaskStatus.NEW);
            given(taskMapper.toEntity(request)).willReturn(taskEntity);
            given(taskMapper.toDto(taskEntity)).willReturn(taskDto);
            var result = taskService.createTask(request);
            assertThat(result).isEqualTo(taskDto);
            assertThat(result.status()).isEqualTo(TaskStatus.NEW);
            verify(taskRepository).save(taskEntity);
            verify(taskMapper).toEntity(request);
            verify(taskMapper).toDto(taskEntity);
        }

        @Test
        @DisplayName("должен выбросить исключение при пустом названии")
        void shouldThrowWhenTitleIsEmpty() {
            
            var request = new CreateTaskRequest("", "Описание");

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(ValidationConflictException.class)
                    .hasMessage("Task title cannot be blank");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("должен выбросить исключение при названии из пробелов")
        void shouldThrowWhenTitleIsBlank() {
            var request = new CreateTaskRequest("   ", "Описание");
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(ValidationConflictException.class)
                    .hasMessage("Task title cannot be blank");
            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTaskById")
    class GetTaskByIdTests {
        @Test
        @DisplayName("должен вернуть задачу при наличии")
        void shouldReturnTaskWhenFound() {
            
            Long taskId = 1L;
            var taskEntity = new Task();
            taskEntity.setId(taskId);
            taskEntity.setTitle("Найдена");
            taskEntity.setStatus(TaskStatus.IN_PROGRESS);
            var taskDto = new TaskDto(taskId, "Найдена", "", TaskStatus.IN_PROGRESS);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(taskEntity));
            given(taskMapper.toDto(taskEntity)).willReturn(taskDto);
            var result = taskService.getTaskById(taskId);
            assertThat(result).isEqualTo(taskDto);
            verify(taskRepository).findById(taskId);
            verify(taskMapper).toDto(taskEntity);
        }

        @Test
        @DisplayName("должен выбросить ResourceNotFoundException когда задача не найдена")
        void shouldThrowWhenTaskNotFound() {
            Long taskId = 999L;
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> taskService.getTaskById(taskId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Task not found with id: 999");
            verify(taskRepository).findById(taskId);
            verify(taskMapper, never()).toDto(any());
        }
    }

    @Nested
    @DisplayName("updateTaskStatus")
    class UpdateTaskStatusTests {
        @Test
        @DisplayName("должен обновить статус задачи")
        void shouldUpdateTaskStatus() {
            Long taskId = 1L;
            var taskEntity = new Task();
            taskEntity.setId(taskId);
            taskEntity.setStatus(TaskStatus.NEW);
            var request = new UpdateStatusRequest(TaskStatus.IN_PROGRESS);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(taskEntity));
            taskService.updateTaskStatus(taskId, request);
            verify(taskRepository).setStatusById(taskId, TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("должен выбросить исключение если задача не найдена")
        void shouldThrowWhenTaskNotFound() {
            Long taskId = 999L;
            var request = new UpdateStatusRequest(TaskStatus.DONE);
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(taskRepository, never()).setStatusById(any(), any());
        }

        @Test
        @DisplayName("должен запретить изменение статуса у выполненной задачи")
        void shouldPreventStatusChangeOnDoneTask() {
            Long taskId = 1L;
            var taskEntity = new Task();
            taskEntity.setId(taskId);
            taskEntity.setStatus(TaskStatus.DONE);
            var request = new UpdateStatusRequest(TaskStatus.IN_PROGRESS);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(taskEntity));
            assertThatThrownBy(() -> taskService.updateTaskStatus(taskId, request))
                    .isInstanceOf(ValidationConflictException.class)
                    .hasMessage("Cannot change status of a completed task (DONE)");
            verify(taskRepository, never()).setStatusById(any(), any());
        }

        @Test
        @DisplayName("должен разрешить установку статуса DONE")
        void shouldAllowTransitionToDone() {
            Long taskId = 1L;
            var taskEntity = new Task();
            taskEntity.setId(taskId);
            taskEntity.setStatus(TaskStatus.IN_PROGRESS);
            var request = new UpdateStatusRequest(TaskStatus.DONE);
            given(taskRepository.findById(taskId)).willReturn(Optional.of(taskEntity));
            taskService.updateTaskStatus(taskId, request);
            verify(taskRepository).setStatusById(taskId, TaskStatus.DONE);
        }
    }
}