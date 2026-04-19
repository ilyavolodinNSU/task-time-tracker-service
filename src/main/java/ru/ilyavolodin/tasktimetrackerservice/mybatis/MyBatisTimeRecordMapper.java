package ru.ilyavolodin.tasktimetrackerservice.mybatis;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import ru.ilyavolodin.tasktimetrackerservice.entity.TimeRecord;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MyBatisTimeRecordMapper {
    @Insert("""
        insert into time_records 
            (employee_id, task_id, start_time, end_time, work_description)
        values 
            (#{employeeId}, #{taskId}, #{startTime}, #{endTime}, #{workDescription})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TimeRecord record);

    @Select("""
        select * from time_records
        where employee_id = #{employeeId}
            and start_time >= #{periodStart} 
            and end_time <= #{periodEnd}
    """)
    List<TimeRecord> select(
        @Param("employeeId") Long employeeId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
}
