package ru.ilyavolodin.tasktimetrackerservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTimeRecordRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TimeRecordDto;
import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimeRecordMapper {
    public TimeRecord toEntity(TimeRecordDto dto);

    public TimeRecordDto toDto(TimeRecord entity);

    public TimeRecord toEntity(CreateTimeRecordRequest request);
}
