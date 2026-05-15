package com.nhnacademy.taskAPI.repository;

import com.nhnacademy.taskAPI.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProject_ProjectId(Long projectId);
    Optional<Milestone> findByMilestoneIdAndProject_ProjectId(Long milestoneId, Long projectId);
    boolean existsByProject_ProjectIdAndName(Long projectId, String name);
}
