package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Comment;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.CommentRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.task.CommentCreateRequest;
import com.nhnacademy.taskAPI.task.CommentDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<CommentDto> getComments(Long projectId, Long taskId, String userId) {
        validateProjectMember(projectId, userId);
        getTaskInProject(projectId, taskId);

        return commentRepository.findByTask_TaskId(taskId)
                .stream()
                .map(this::toCommentDto)
                .toList();
    }

    @Transactional
    public CommentDto createComment(Long projectId, Long taskId, CommentCreateRequest request, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);

        Task task = getTaskInProject(projectId, taskId);
        Comment comment = new Comment(task, userId, request.content());

        return toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto updateComment(Long projectId, Long taskId, Long commentId, CommentCreateRequest request, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);
        getTaskInProject(projectId, taskId);

        Comment comment = getCommentInTask(taskId, commentId);
        validateCommentWriter(comment, userId);
        comment.updateContent(request.content());

        return toCommentDto(comment);
    }

    @Transactional
    public void deleteComment(Long projectId, Long taskId, Long commentId, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);
        getTaskInProject(projectId, taskId);

        Comment comment = getCommentInTask(taskId, commentId);
        validateCommentWriter(comment, userId);
        commentRepository.delete(comment);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private void validateProjectMember(Long projectId, String userId) {
        if (!projectMemberRepository.existsByProject_ProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    private void validateProjectWritable(Project project) {
        if (project.getStatus() == ProjectStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_ACTIVE);
        }
    }

    private Task getTaskInProject(Long projectId, Long taskId) {
        return taskRepository.findByTaskIdAndProject_ProjectId(taskId, projectId)
                .orElseThrow(() -> {
                    if (taskRepository.existsById(taskId)) {
                        return new BusinessException(ErrorCode.TASK_NOT_IN_PROJECT);
                    }
                    return new BusinessException(ErrorCode.TASK_NOT_FOUND);
                });
    }

    private Comment getCommentInTask(Long taskId, Long commentId) {
        return commentRepository.findByCommentIdAndTask_TaskId(commentId, taskId)
                .orElseThrow(() -> {
                    if (commentRepository.existsById(commentId)) {
                        return new BusinessException(ErrorCode.COMMENT_NOT_IN_TASK);
                    }
                    return new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
                });
    }

    private void validateCommentWriter(Comment comment, String userId) {
        if (!comment.isWriter(userId)) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_WRITER);
        }
    }

    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getCommentId(),
                comment.getWriterId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
