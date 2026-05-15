package com.nhnacademy.taskAPI.repository;

import com.nhnacademy.taskAPI.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByProject_ProjectId(Long projectId);
    Optional<Tag> findByTagIdAndProject_ProjectId(Long tagId, Long projectId);
    boolean existsByProject_ProjectIdAndName(Long projectId, String name);
}
