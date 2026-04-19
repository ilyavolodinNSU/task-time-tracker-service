package ru.ilyavolodin.tasktimetrackerservice.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;
import ru.ilyavolodin.tasktimetrackerservice.mybatis.MyBatisTaskMapper;

@Repository
@RequiredArgsConstructor
public class MyBatisTaskRepositoryImpl implements TaskRepository {
    private final MyBatisTaskMapper myBatisTaskMapper;

    @Override
    public void save(Task task) {
        myBatisTaskMapper.insert(task);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(myBatisTaskMapper.select(id));
    }

    @Override
    public void setStatusById(Long id, TaskStatus status) {
        myBatisTaskMapper.update(id, status);
    }
}
