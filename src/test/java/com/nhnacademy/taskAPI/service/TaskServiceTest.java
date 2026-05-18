package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Milestone;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Tag;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.entity.TaskTag;
import com.nhnacademy.taskAPI.entity.Comment;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.CommentRepository;
import com.nhnacademy.taskAPI.repository.MilestoneRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TagRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.repository.TaskTagRepository;
import com.nhnacademy.taskAPI.task.MilestoneCreateRequest;
import com.nhnacademy.taskAPI.task.MilestoneDto;
import com.nhnacademy.taskAPI.task.TagCreateRequest;
import com.nhnacademy.taskAPI.task.TagDto;
import com.nhnacademy.taskAPI.task.TaskCreateRequest;
import com.nhnacademy.taskAPI.task.TaskDetailDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskMilestoneRequest;
import com.nhnacademy.taskAPI.task.TaskUpdateRequest;
import java.time.LocalDate;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskTagRepository taskTagRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TagService tagService;

    @Mock
    private MilestoneService milestoneService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTasksReturnsProjectTasksWhenTagIdIsNull() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project, null);
        Tag backend = tag(100L, project, "Backend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByProject_ProjectId(1L)).thenReturn(List.of(task));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of(new TaskTag(task, backend)));

        List<TaskDto> response = taskService.getTasks(1L, null, "user1");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().taskId()).isEqualTo(20L);
        assertThat(response.getFirst().tags()).extracting("name").containsExactly("Backend");
    }

    @Test
    void getTasksFiltersByTagId() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project, null);
        Tag backend = tag(100L, project, "Backend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.findAllByTagIdIn(anyCollection())).thenReturn(List.of(backend));
        when(taskTagRepository.findByTask_Project_ProjectIdAndTag_TagId(1L, 100L))
                .thenReturn(List.of(new TaskTag(task, backend)));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of(new TaskTag(task, backend)));

        List<TaskDto> response = taskService.getTasks(1L, 100L, "user1");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().taskId()).isEqualTo(20L);
        assertThat(response.getFirst().tags()).extracting("tagId").containsExactly(100L);
    }

    @Test
    void getTasksRejectsFilterTagInOtherProject() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Project otherProject = project(2L, ProjectStatus.ACTIVE);
        Tag otherTag = tag(100L, otherProject, "Other");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.findAllByTagIdIn(anyCollection())).thenReturn(List.of(otherTag));

        assertThatThrownBy(() -> taskService.getTasks(1L, 100L, "user1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TAG_NOT_IN_PROJECT);
    }

    @Test
    void getTaskReturnsDetailWithMilestoneTagsAndComments() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project);
        Task task = task(20L, project, milestone);
        Tag backend = tag(100L, project, "Backend");
        Comment comment = comment(30L, task, "user2", "Comment");

        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByTask_TaskId(20L)).thenReturn(List.of(comment));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of(new TaskTag(task, backend)));

        TaskDetailDto response = taskService.getTask(1L, 20L, "user1");

        assertThat(response.taskId()).isEqualTo(20L);
        assertThat(response.milestone().milestoneId()).isEqualTo(10L);
        assertThat(response.tags()).extracting("name").containsExactly("Backend");
        assertThat(response.comments()).extracting("content").containsExactly("Comment");
    }

    @Test
    void createTaskAssignsExistingMilestoneAndTags() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project);
        Tag backend = tag(100L, project, "Backend");
        Tag urgent = tag(101L, project, "Urgent");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));
        when(tagRepository.findAllByTagIdIn(anyCollection())).thenReturn(List.of(backend, urgent));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "taskId", 20L);
            return task;
        });
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of(
                new TaskTag(task(20L, project, milestone), backend),
                new TaskTag(task(20L, project, milestone), urgent)
        ));

        TaskCreateRequest request = new TaskCreateRequest(null, 1L, "Task", "Content", "ignored", null);

        TaskDto response = taskService.createTask(
                1L,
                request,
                10L,
                null,
                null,
                null,
                List.of(100L, 101L),
                null,
                "user1"
        );

        assertThat(response.taskId()).isEqualTo(20L);
        assertThat(response.milestoneId()).isEqualTo(10L);
        assertThat(response.writerId()).isEqualTo("user1");
        assertThat(response.tags()).extracting("name").containsExactly("Backend", "Urgent");
        verify(taskTagRepository).saveAll(any());
    }

    @Test
    void createTaskCreatesAndAssignsNewMilestoneAndNewTag() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project);
        Tag backend = tag(100L, project, "Backend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneService.createMilestone(
                eq(1L),
                eq("user1"),
                any(MilestoneCreateRequest.class)
        )).thenReturn(new MilestoneDto(
                10L,
                "Sprint 1",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 20)
        ));
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));
        when(tagService.createTag(eq(1L), eq("user1"), any(TagCreateRequest.class)))
                .thenReturn(new TagDto(100L, "Backend"));
        when(tagRepository.findAllByTagIdIn(anyCollection())).thenReturn(List.of(backend));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "taskId", 20L);
            return task;
        });
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of(new TaskTag(
                task(20L, project, milestone),
                backend
        )));

        TaskCreateRequest request = new TaskCreateRequest(null, 1L, "Task", "Content", null, null);

        TaskDto response = taskService.createTask(
                1L,
                request,
                null,
                "Sprint 1",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 20),
                null,
                "Backend",
                "user1"
        );

        assertThat(response.milestoneId()).isEqualTo(10L);
        assertThat(response.tags()).extracting("name").containsExactly("Backend");
        verify(milestoneService).createMilestone(eq(1L), eq("user1"), any(MilestoneCreateRequest.class));
        verify(tagService).createTag(eq(1L), eq("user1"), any(TagCreateRequest.class));
        verify(taskTagRepository).saveAll(any());
    }

    @Test
    void updateTaskChangesTitleAndContentButKeepsWriter() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project, null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of());

        TaskDto response = taskService.updateTask(
                1L,
                20L,
                new TaskUpdateRequest("Updated", "Updated content"),
                "user1"
        );

        assertThat(response.title()).isEqualTo("Updated");
        assertThat(response.content()).isEqualTo("Updated content");
        assertThat(response.writerId()).isEqualTo("user1");
    }

    @Test
    void deleteTaskDeletesTaskInProject() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project, null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, 20L, "user1");

        verify(taskRepository).delete(task);
    }

    @Test
    void updateTaskMilestoneRemovesMilestoneWhenRequestMilestoneIdIsNull() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project);
        Task task = task(20L, project, milestone);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of());

        TaskDto response = taskService.updateTaskMilestone(
                1L,
                20L,
                new TaskMilestoneRequest(null),
                "user1"
        );

        assertThat(task.getMilestone()).isNull();
        assertThat(response.milestoneId()).isNull();
    }

    @Test
    void updateTaskMilestoneAssignsExistingMilestone() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project, null);
        Milestone milestone = milestone(10L, project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of());

        TaskDto response = taskService.updateTaskMilestone(
                1L,
                20L,
                new TaskMilestoneRequest(10L),
                "user1"
        );

        assertThat(task.getMilestone()).isEqualTo(milestone);
        assertThat(response.milestoneId()).isEqualTo(10L);
    }

    @Test
    void getTaskRejectsNonProjectMember() {
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(false);

        assertThatThrownBy(() -> taskService.getTask(1L, 20L, "user1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PROJECT_MEMBER);
    }

    @Test
    void updateTaskRejectsTaskInOtherProject() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.empty());
        when(taskRepository.existsById(20L)).thenReturn(true);

        assertThatThrownBy(() -> taskService.updateTask(
                1L,
                20L,
                new TaskUpdateRequest("Updated", "Updated content"),
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TASK_NOT_IN_PROJECT);
    }

    @Test
    void createTaskRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        TaskCreateRequest request = new TaskCreateRequest(null, 1L, "Task", "Content", null, null);

        assertThatThrownBy(() -> taskService.createTask(
                1L,
                request,
                null,
                null,
                null,
                null,
                null,
                null,
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

    private Milestone milestone(Long milestoneId, Project project) {
        Milestone milestone = new Milestone(project, "Sprint", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 20));
        ReflectionTestUtils.setField(milestone, "milestoneId", milestoneId);
        return milestone;
    }

    private Tag tag(Long tagId, Project project, String name) {
        Tag tag = new Tag(project, name);
        ReflectionTestUtils.setField(tag, "tagId", tagId);
        return tag;
    }

    private Task task(Long taskId, Project project, Milestone milestone) {
        Task task = new Task(project, milestone, "Task", "Content", "user1");
        ReflectionTestUtils.setField(task, "taskId", taskId);
        return task;
    }

    private Comment comment(Long commentId, Task task, String writerId, String content) {
        Comment comment = new Comment(task, writerId, content);
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        return comment;
    }
}
