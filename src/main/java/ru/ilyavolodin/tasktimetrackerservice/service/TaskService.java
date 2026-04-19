package ru.ilyavolodin.tasktimetrackerservice.service;

import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTaskRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TaskDto;
import ru.ilyavolodin.tasktimetrackerservice.dto.UpdateStatusRequest;

public interface TaskService {
    public TaskDto createTask(CreateTaskRequest request);

    public TaskDto getTaskById(Long id);

    public void updateTaskStatus(Long id, UpdateStatusRequest request);
}
