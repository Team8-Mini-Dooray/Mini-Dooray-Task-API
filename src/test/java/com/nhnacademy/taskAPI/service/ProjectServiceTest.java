package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Milestone;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectMember;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Tag;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.entity.TaskTag;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.MilestoneRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.repository.TaskTagRepository;
import com.nhnacademy.taskAPI.task.*;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private TaskTagRepository taskTagRepository;

    @Mock
    private TaskTagService taskTagService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getProjectsReturnsOnlyUserProjects() {
        Project project1 = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");
        Project project2 = project(2L, "Project B", ProjectStatus.DORMANT, "admin");

        when(projectMemberRepository.findByUserId("user1")).thenReturn(List.of(
                new ProjectMember(project1, "user1"),
                new ProjectMember(project2, "user1")
        ));

        List<ProjectDto> response = projectService.getProjects("user1");

        assertThat(response).hasSize(2);
        assertThat(response).extracting("name").containsExactly("Project A", "Project B");
        assertThat(response).extracting("status").containsExactly("ACTIVE", "DORMANT");
    }

    @Test
    void getProjectDetailReturnsMembersTasksMilestonesAndTaskTags() {
        Project project = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");
        ProjectMember admin = new ProjectMember(project, "admin");
        ProjectMember user1 = new ProjectMember(project, "user1");

        Milestone milestone = milestone(10L, project, "Sprint 1");
        Task task = task(20L, project, milestone);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(projectMemberRepository.findByProject_ProjectId(1L)).thenReturn(List.of(admin, user1));
        when(taskRepository.findByProject_ProjectId(1L)).thenReturn(List.of(task));
        when(milestoneRepository.findByProject_ProjectId(1L)).thenReturn(List.of(milestone));
        when(taskTagService.getTaskTags(20L)).thenReturn(List.of(new TagDto(100L, "Bug")));

        ProjectDetailDto response = projectService.getProjectDetail(1L, "user1");

        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Project A");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.adminId()).isEqualTo("admin");

        assertThat(response.members()).hasSize(2);
        assertThat(response.members()).extracting("userId").containsExactly("admin", "user1");

        assertThat(response.tasks()).hasSize(1);
        assertThat(response.tasks().get(0).taskId()).isEqualTo(20L);
        assertThat(response.tasks().get(0).milestoneId()).isEqualTo(10L);
        assertThat(response.tasks().get(0).tags()).hasSize(1);
        assertThat(response.tasks().get(0).tags().get(0).name()).isEqualTo("Bug");

        assertThat(response.milestones()).hasSize(1);
        assertThat(response.milestones().get(0).name()).isEqualTo("Sprint 1");
    }

    @Test
    void createProjectCreatesProjectAndRegistersCreatorAsMember() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            ReflectionTestUtils.setField(project, "projectId", 1L);
            return project;
        });

        ProjectDto response = projectService.createProject(
                "user1",
                new ProjectCreateRequest("New Project")
        );

        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("New Project");
        assertThat(response.status()).isEqualTo("ACTIVE");

        verify(projectRepository).save(any(Project.class));
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void updateProjectUpdatesNameAndStatus() {
        Project project = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectDto response = projectService.updateProject(
                1L,
                "admin",
                new ProjectUpdateRequest("Project Updated", "DORMANT")
        );

        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Project Updated");
        assertThat(response.status()).isEqualTo("DORMANT");
        assertThat(project.getName()).isEqualTo("Project Updated");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.DORMANT);
    }

    @Test
    void updateProjectRejectsNonAdmin() {
        Project project = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(
                1L,
                "user1",
                new ProjectUpdateRequest("Project Updated", "DORMANT")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PROJECT_ADMIN);
    }

    @Test
    void updateProjectRejectsInvalidStatus() {
        Project project = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(
                1L,
                "admin",
                new ProjectUpdateRequest("Project Updated", "INVALID")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PROJECT_STATUS);
    }

    @Test
    void closeProjectChangesStatusToTerminated() {
        Project project = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectDto response = projectService.closeProject(1L, "admin");

        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("TERMINATED");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.TERMINATED);
    }

    @Test
    void closeProjectRejectsNonAdmin() {
        Project project = project(1L, "Project A", ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.closeProject(1L, "user1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PROJECT_ADMIN);
    }

    private Project project(Long projectId, String name, ProjectStatus status, String adminId) {
        Project project = new Project(name, status, adminId);
        ReflectionTestUtils.setField(project, "projectId", projectId);
        return project;
    }

    private Milestone milestone(Long milestoneId, Project project, String name) {
        Milestone milestone = new Milestone(
                project,
                name,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 15)
        );
        ReflectionTestUtils.setField(milestone, "milestoneId", milestoneId);
        return milestone;
    }

    private Task task(Long taskId, Project project, Milestone milestone) {
        Task task = new Task(project, milestone, "Task 1", "Content", "user1");
        ReflectionTestUtils.setField(task, "taskId", taskId);
        return task;
    }

    private Tag tag(Long tagId, Project project, String name) {
        Tag tag = new Tag(project, name);
        ReflectionTestUtils.setField(tag, "tagId", tagId);
        return tag;
    }
}