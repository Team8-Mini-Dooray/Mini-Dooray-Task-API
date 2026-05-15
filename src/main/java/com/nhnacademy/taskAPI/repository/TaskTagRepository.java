package com.nhnacademy.taskAPI.repository;

import com.nhnacademy.taskAPI.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {

    void deleteAllByTask_TaskId(Long taskId);

    List<TaskTag> findByTask_TaskId(Long taskId);


}
