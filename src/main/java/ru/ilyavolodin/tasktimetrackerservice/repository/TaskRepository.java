package ru.ilyavolodin.tasktimetrackerservice.repository;

import java.util.Optional;

import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;

public interface TaskRepository {
    public void save(Task task);

    public Optional<Task> findById(Long id);

    public void setStatusById(Long id, TaskStatus status);
}
