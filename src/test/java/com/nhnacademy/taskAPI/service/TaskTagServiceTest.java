package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Tag;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.entity.TaskTag;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TagRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.repository.TaskTagRepository;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskTagRequest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskTagServiceTest {

    @Mock
    private TaskTagRepository taskTagRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TaskTagService taskTagService;

    @Test
    void updateTaskTagsReplacesAllTaskTags() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);
        Tag backend = tag(100L, project, "Backend");
        Tag urgent = tag(101L, project, "Urgent");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(tagRepository.findAllByTagIdIn(anyCollection())).thenReturn(List.of(backend, urgent));
        when(taskTagRepository.findByTask_TaskId(20L)).thenReturn(List.of(
                new TaskTag(task, backend),
                new TaskTag(task, urgent)
        ));

        TaskDto response = taskTagService.updateTaskTags(
                1L,
                20L,
                new TaskTagRequest(List.of(100L, 101L)),
                "user1"
        );

        assertThat(response.taskId()).isEqualTo(20L);
        assertThat(response.tags()).extracting("name").containsExactly("Backend", "Urgent");
        verify(taskTagRepository).deleteAllByTask_TaskId(20L);
        verify(taskTagRepository).flush();
        verify(taskTagRepository).saveAll(any());
    }

    @Test
    void updateTaskTagsRequiresAtLeastOneTag() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskTagService.updateTaskTags(
                1L,
                20L,
                new TaskTagRequest(List.of()),
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TASK_TAG_REQUIRED);
    }

    @Test
    void updateTaskTagsRejectsTagInOtherProject() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Project otherProject = project(2L, ProjectStatus.ACTIVE);
        Task task = task(20L, project);
        Tag otherTag = tag(100L, otherProject, "Other");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(taskRepository.findByTaskIdAndProject_ProjectId(20L, 1L)).thenReturn(Optional.of(task));
        when(tagRepository.findAllByTagIdIn(anyCollection())).thenReturn(List.of(otherTag));

        assertThatThrownBy(() -> taskTagService.updateTaskTags(
                1L,
                20L,
                new TaskTagRequest(List.of(100L)),
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TAG_NOT_IN_PROJECT);
    }

    @Test
    void updateTaskTagsRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> taskTagService.updateTaskTags(
                1L,
                20L,
                new TaskTagRequest(List.of(100L)),
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

    private Tag tag(Long tagId, Project project, String name) {
        Tag tag = new Tag(project, name);
        ReflectionTestUtils.setField(tag, "tagId", tagId);
        return tag;
    }
}
