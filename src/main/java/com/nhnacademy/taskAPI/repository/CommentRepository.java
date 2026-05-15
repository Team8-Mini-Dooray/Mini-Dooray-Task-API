package com.nhnacademy.taskAPI.repository;

import com.nhnacademy.taskAPI.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask_TaskId(Long taskId);

    Optional<Comment> findByCommentIdAndTask_TaskId(Long commentId, Long taskId);
}
