package ru.ilyavolodin.tasktimetrackerservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTaskRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TaskDto;
import ru.ilyavolodin.tasktimetrackerservice.entity.Task;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    public Task toEntity(TaskDto dto);

    public TaskDto toDto(Task entity);

    public Task toEntity(CreateTaskRequest request);
}
