package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Milestone;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Tag;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.entity.TaskTag;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.CommentRepository;
import com.nhnacademy.taskAPI.repository.MilestoneRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TagRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.repository.TaskTagRepository;
import com.nhnacademy.taskAPI.task.TaskCreateRequest;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskMilestoneRequest;
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
import static org.mockito.Mockito.never;
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

    @InjectMocks
    private TaskService taskService;

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
    void createTaskLeavesNewMilestoneAndNewTagAsTodo() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "taskId", 20L);
            return task;
        });
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of());

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

        assertThat(response.milestoneId()).isNull();
        assertThat(response.tags()).isEmpty();
        verify(milestoneRepository, never()).save(any(Milestone.class));
        verify(tagRepository, never()).save(any(Tag.class));
        verify(taskTagRepository, never()).saveAll(any());
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
}
