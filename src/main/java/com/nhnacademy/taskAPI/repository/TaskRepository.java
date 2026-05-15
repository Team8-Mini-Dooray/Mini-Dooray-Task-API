package com.nhnacademy.taskAPI.repository;

import com.nhnacademy.taskAPI.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject_ProjectId(Long projectId);

    List<Task> findByMilestone_MilestoneIdAndProject_ProjectId(Long milestoneId, Long projectId);
}