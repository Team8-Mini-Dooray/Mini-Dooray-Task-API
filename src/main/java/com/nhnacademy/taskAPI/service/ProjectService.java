package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectMember;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.MilestoneRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.task.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;

    public List<ProjectDto> getProjects(String userId) {
        validateUserId(userId);
        return projectMemberRepository.findByUserId(userId)
                .stream()
                .map(ProjectMember::getProject)
                .map(this::toProjectDto)
                .toList();
    }
    public ProjectDetailDto getProjectDetail(Long projectId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);

        List<ProjectMemberDto> members = projectMemberRepository.findByProject_ProjectId(projectId)
                .stream()
                .map(member -> new ProjectMemberDto(member.getUserId()))
                .toList();

        List<TaskDto> tasks = taskRepository.findByProject_ProjectId(projectId)
                .stream()
                .map(task -> new TaskDto (
                        task.getTaskId(),
                        task.getMilestone() == null ? null : task.getMilestone().getMilestoneId(),
                        task.getTitle(),
                        task.getContent(),
                        task.getWriterId(),
                        task.getCreatedAt(),
                        List.of()
                ))
                .toList();

        List<MilestoneDto> milestones = milestoneRepository.findByProject_ProjectId(projectId)
                .stream()
                .map(milestone -> new MilestoneDto(
                        milestone.getMilestoneId(),
                        milestone.getName(),
                        milestone.getStartDate(),
                        milestone.getEndDate()
                ))
                .toList();
        return new ProjectDetailDto(
                project.getProjectId(),
                project.getName(),
                project.getStatus().name(),
                project.getAdminId(),
                members,
                tasks,
                milestones

        );
    }
    @Transactional
    public ProjectDto createProject(String userId, ProjectCreateRequest request) {
        validateUserId(userId);
        Project project = new Project(
                request.name(),
                ProjectStatus.ACTIVE,
                userId
        );
        Project savedProject = projectRepository.save(project);

        ProjectMember adminMember = new ProjectMember(savedProject, userId);
        projectMemberRepository.save(adminMember);
        return toProjectDto(savedProject);
    }
    @Transactional
    public ProjectDto updateProject(Long projectId, String userId, ProjectUpdateRequest request) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectAdmin(project, userId);

        ProjectStatus status = parseProjectStatus(request.status());
        project.updateName(request.name());
        project.updateStatus(status);

        return toProjectDto(project);
    }
    @Transactional
    public ProjectDto closeProject(Long projectId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectAdmin(project, userId);

        project.updateStatus(ProjectStatus.TERMINATED);
        return toProjectDto(project);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_USER_ID);
        }
    }
    private void validateProjectMember(Long projectId, String userId) {
        if (!projectMemberRepository.existsByProject_ProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }
    private void validateProjectAdmin(Project project, String userId) {
        if (!project.getAdminId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_ADMIN);
        }
    }
    private void validateProjectWritable(Project project) {
        if (project.getStatus() == ProjectStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_ACTIVE);
        }
    }
    private ProjectStatus parseProjectStatus(String status) {
        try {
            return ProjectStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS);
        }
    }

    private ProjectDto toProjectDto(Project project) {
        return new ProjectDto(
                project.getProjectId(),
                project.getName(),
                project.getStatus().name()
        );
    }
}
