package ru.ilyavolodin.tasktimetrackerservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTaskRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TaskDto;
import ru.ilyavolodin.tasktimetrackerservice.dto.UpdateStatusRequest;
import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;
import ru.ilyavolodin.tasktimetrackerservice.exception.ResourceNotFoundException;
import ru.ilyavolodin.tasktimetrackerservice.exception.ValidationConflictException;
import ru.ilyavolodin.tasktimetrackerservice.mapper.TaskMapper;
import ru.ilyavolodin.tasktimetrackerservice.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        if (request.title().isBlank()) throw new ValidationConflictException("Task title cannot be blank");

        Task entity = taskMapper.toEntity(request);
        entity.setStatus(TaskStatus.NEW);
        taskRepository.save(entity);
        return taskMapper.toDto(entity);
    }

    @Override
    public TaskDto getTaskById(Long id) {
        return taskRepository.findById(id)
            .map(taskMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long id, UpdateStatusRequest request) {
        var task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (task.getStatus() == TaskStatus.DONE && request.status() != TaskStatus.DONE)
            throw new ValidationConflictException("Cannot change status of a completed task (DONE)");

        taskRepository.setStatusById(id, request.status());
    }
}
