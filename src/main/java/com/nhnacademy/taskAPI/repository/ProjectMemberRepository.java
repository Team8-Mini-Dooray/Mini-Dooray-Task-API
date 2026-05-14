package com.nhnacademy.taskAPI.repository;

import com.nhnacademy.taskAPI.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProject_ProjectIdAndUserId(Long projectId, String userId);
    Optional<ProjectMember> findByProject_ProjectIdAndUserId(Long projectId, String userId);
    List<ProjectMember> findByProject_ProjectId(Long projectId);
    List<ProjectMember> findByUserId(String userId);
}
