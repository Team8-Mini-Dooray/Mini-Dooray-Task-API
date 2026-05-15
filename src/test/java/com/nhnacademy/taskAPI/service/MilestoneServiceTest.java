package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Milestone;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.MilestoneRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.task.MilestoneCreateRequest;
import com.nhnacademy.taskAPI.task.MilestoneDetailDto;
import com.nhnacademy.taskAPI.task.MilestoneDto;
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
class MilestoneServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private MilestoneService milestoneService;

    @Test
    void getMilestonesReturnsProjectMilestones() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone sprint1 = milestone(10L, project, "Sprint 1");
        Milestone sprint2 = milestone(11L, project, "Sprint 2");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.findByProject_ProjectId(1L)).thenReturn(List.of(sprint1, sprint2));

        List<MilestoneDto> response = milestoneService.getMilestones(1L, "user1");

        assertThat(response).hasSize(2);
        assertThat(response).extracting("name").containsExactly("Sprint 1", "Sprint 2");
    }

    @Test
    void getMilestoneDetailReturnsMilestoneAndTasks() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project, "Sprint 1");
        Task task = task(20L, project, milestone);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));
        when(taskRepository.findByMilestone_MilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(List.of(task));

        MilestoneDetailDto response = milestoneService.getMilestoneDetail(1L, 10L, "user1");

        assertThat(response.milestoneId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Sprint 1");
        assertThat(response.tasks()).hasSize(1);
        assertThat(response.tasks().get(0).taskId()).isEqualTo(20L);
        assertThat(response.tasks().get(0).milestoneId()).isEqualTo(10L);
    }

    @Test
    void createMilestoneCreatesMilestone() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.existsByProject_ProjectIdAndName(1L, "Sprint 1")).thenReturn(false);
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> {
            Milestone milestone = invocation.getArgument(0);
            ReflectionTestUtils.setField(milestone, "milestoneId", 10L);
            return milestone;
        });

        MilestoneDto response = milestoneService.createMilestone(
                1L,
                "user1",
                new MilestoneCreateRequest(
                        "Sprint 1",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 15)
                )
        );

        assertThat(response.milestoneId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Sprint 1");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        verify(milestoneRepository).save(any(Milestone.class));
    }

    @Test
    void createMilestoneRejectsDuplicateName() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.existsByProject_ProjectIdAndName(1L, "Sprint 1")).thenReturn(true);

        assertThatThrownBy(() -> milestoneService.createMilestone(
                1L,
                "user1",
                new MilestoneCreateRequest(
                        "Sprint 1",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 15)
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_MILESTONE_NAME);
    }

    @Test
    void createMilestoneRejectsInvalidDateRange() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);

        assertThatThrownBy(() -> milestoneService.createMilestone(
                1L,
                "user1",
                new MilestoneCreateRequest(
                        "Sprint 1",
                        LocalDate.of(2026, 5, 20),
                        LocalDate.of(2026, 5, 10)
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    void createMilestoneRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);

        assertThatThrownBy(() -> milestoneService.createMilestone(
                1L,
                "user1",
                new MilestoneCreateRequest(
                        "Sprint 1",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 15)
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_ACTIVE);
    }

    @Test
    void updateMilestoneUpdatesMilestone() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project, "Sprint 1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));
        when(milestoneRepository.existsByProject_ProjectIdAndName(1L, "Sprint 1 Updated")).thenReturn(false);

        MilestoneDto response = milestoneService.updateMilestone(
                1L,
                10L,
                "user1",
                new MilestoneCreateRequest(
                        "Sprint 1 Updated",
                        LocalDate.of(2026, 5, 2),
                        LocalDate.of(2026, 5, 16)
                )
        );

        assertThat(response.milestoneId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Sprint 1 Updated");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 5, 16));
        assertThat(milestone.getName()).isEqualTo("Sprint 1 Updated");
    }

    @Test
    void updateMilestoneRejectsDuplicateName() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project, "Sprint 1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));
        when(milestoneRepository.existsByProject_ProjectIdAndName(1L, "Sprint 2")).thenReturn(true);

        assertThatThrownBy(() -> milestoneService.updateMilestone(
                1L,
                10L,
                "user1",
                new MilestoneCreateRequest(
                        "Sprint 2",
                        LocalDate.of(2026, 5, 2),
                        LocalDate.of(2026, 5, 16)
                )
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_MILESTONE_NAME);
    }

    @Test
    void deleteMilestoneDeletesMilestone() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Milestone milestone = milestone(10L, project, "Sprint 1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(milestoneRepository.findByMilestoneIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(milestone));

        milestoneService.deleteMilestone(1L, 10L, "user1");

        verify(milestoneRepository).delete(milestone);
    }

    private Project project(Long projectId, ProjectStatus status) {
        Project project = new Project("Project", status, "admin");
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
        Task task = new Task(project, milestone, "Task", "Content", "user1");
        ReflectionTestUtils.setField(task, "taskId", taskId);
        return task;
    }
}