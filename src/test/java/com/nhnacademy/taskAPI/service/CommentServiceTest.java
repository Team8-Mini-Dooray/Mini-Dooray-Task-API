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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getCommentsReturnsTaskComments() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);
        Comment comment = comment(30L, task, "user1");

        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByTask_TaskId(20L)).thenReturn(List.of(comment));

        List<CommentDto> response = commentService.getComments(1L, 20L, "user1");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().commentId()).isEqualTo(30L);
        assertThat(response.getFirst().content()).isEqualTo("Comment");
    }

    @Test
    void createCommentUsesHeaderUserIdAsWriter() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            ReflectionTestUtils.setField(comment, "commentId", 30L);
            return comment;
        });

        CommentDto response = commentService.createComment(
                1L,
                20L,
                new CommentCreateRequest("Comment"),
                "user1"
        );

        assertThat(response.commentId()).isEqualTo(30L);
        assertThat(response.writerId()).isEqualTo("user1");
        assertThat(response.content()).isEqualTo("Comment");
    }

    @Test
    void updateCommentChangesContent() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);
        Comment comment = comment(30L, task, "user1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByCommentIdAndTask_TaskId(30L, 20L)).thenReturn(Optional.of(comment));

        CommentDto response = commentService.updateComment(
                1L,
                20L,
                30L,
                new CommentCreateRequest("Updated"),
                "user1"
        );

        assertThat(response.content()).isEqualTo("Updated");
        assertThat(comment.getContent()).isEqualTo("Updated");
    }

    @Test
    void updateCommentRequiresCommentWriter() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);
        Comment comment = comment(30L, task, "writer");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "other")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByCommentIdAndTask_TaskId(30L, 20L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(
                1L,
                20L,
                30L,
                new CommentCreateRequest("Updated"),
                "other"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_COMMENT_WRITER);
    }

    @Test
    void updateCommentRejectsCommentNotInTask() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByCommentIdAndTask_TaskId(30L, 20L)).thenReturn(Optional.empty());
        when(commentRepository.existsById(30L)).thenReturn(true);

        assertThatThrownBy(() -> commentService.updateComment(
                1L,
                20L,
                30L,
                new CommentCreateRequest("Updated"),
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_NOT_IN_TASK);
    }

    @Test
    void getCommentsRejectsTaskInOtherProject() {
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.empty());
        when(taskRepository.existsById(20L)).thenReturn(true);

        assertThatThrownBy(() -> commentService.getComments(1L, 20L, "user1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TASK_NOT_IN_PROJECT);
    }

    @Test
    void createCommentRejectsNonProjectMember() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(false);

        assertThatThrownBy(() -> commentService.createComment(
                1L,
                20L,
                new CommentCreateRequest("Comment"),
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PROJECT_MEMBER);
    }

    @Test
    void deleteCommentDeletesOnlyWriterComment() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);
        Comment comment = comment(30L, task, "user1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByCommentIdAndTask_TaskId(30L, 20L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 20L, 30L, "user1");

        verify(commentRepository).delete(comment);
    }

    @Test
    void createCommentRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> commentService.createComment(
                1L,
                20L,
                new CommentCreateRequest("Comment"),
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_ACTIVE);
    }

    private Project project(Long projectId, ProjectStatus status) {
        Project project = new Project("Project", status, "admin");
        ReflectionTestUtils.setField(project, "projectId", projectId);
        return project;
    }

    private Task task(Long taskId, Project project) {
        Task task = new Task(project, null, "Task", "Content", "user1");
        ReflectionTestUtils.setField(task, "taskId", taskId);
        return task;
    }

    private Comment comment(Long commentId, Task task, String writerId) {
        Comment comment = new Comment(task, writerId, "Comment");
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        return comment;
    }
}
