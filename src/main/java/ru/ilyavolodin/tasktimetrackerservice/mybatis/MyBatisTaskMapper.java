package ru.ilyavolodin.tasktimetrackerservice.mybatis;

import org.apache.ibatis.annotations.*;

import ru.ilyavolodin.tasktimetrackerservice.entity.Task;
import ru.ilyavolodin.tasktimetrackerservice.entity.TaskStatus;

@Mapper
public interface MyBatisTaskMapper {
    @Insert("""
        insert into tasks (title, description, status, created_at) 
        values (#{title}, #{description}, #{status}, now())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Task task);

    @Select("select * from tasks where id = #{id}")
    Task select(Long id);

    @Update("update tasks set status = #{status} where id = #{id}")
    void update(@Param("id") Long id, @Param("status") TaskStatus status);
}
