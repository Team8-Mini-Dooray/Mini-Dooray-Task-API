package com.nhnacademy.taskAPI.service;


import com.nhnacademy.taskAPI.entity.Milestone;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.MilestoneRepository;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TaskRepository;
import com.nhnacademy.taskAPI.task.MilestoneCreateRequest;
import com.nhnacademy.taskAPI.task.MilestoneDetailDto;
import com.nhnacademy.taskAPI.task.MilestoneDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;

    public List<MilestoneDto> getMilestones(Long projectId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(project.getProjectId(), userId);

        return milestoneRepository.findByProject_ProjectId(projectId).stream().map(this::toMilestoneDto).toList();
    }
    public MilestoneDetailDto getMilestoneDetail(Long projectId, Long milestoneId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(project.getProjectId(), userId);

        Milestone milestone = getMilestone(projectId, milestoneId);

        List<TaskDto> tasks = taskRepository
                .findByMilestone_MilestoneIdAndProject_ProjectId(milestoneId, projectId)
                .stream()
                .map(task -> new TaskDto(
                        task.getTaskId(),
                        task.getMilestone() == null ? null : task.getMilestone().getMilestoneId(),
                        task.getTitle(),
                        task.getContent(),
                        task.getWriterId(),
                        task.getCreatedAt(),
                        List.of()
                ))
                .toList();

        return new MilestoneDetailDto(
                milestone.getMilestoneId(),
                milestone.getName(),
                milestone.getStartDate(),
                milestone.getEndDate(),
                tasks
        );
    }
    @Transactional
    public MilestoneDto createMilestone(Long projectId, String userId, MilestoneCreateRequest request){
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);
        validateProjectWritable(project);
        validateDateRange(request.startDate(), request.endDate());
        if (milestoneRepository.existsByProject_ProjectIdAndName(projectId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MILESTONE_NAME);
        }
        Milestone milestone = new Milestone(
                project, request.name(), request.startDate(), request.endDate()
        );
        Milestone savedMilestone = milestoneRepository.save(milestone);
        return toMilestoneDto(savedMilestone);
    }
    @Transactional
    public MilestoneDto updateMilestone(
            Long projectId,
            Long milestoneId,
            String userId,
            MilestoneCreateRequest request
    ) {
        validateUserId(userId);
        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);
        validateProjectWritable(project);
        validateDateRange(request.startDate(), request.endDate());

        Milestone milestone = getMilestone(projectId, milestoneId);
        if (!milestone.getName().equals(request.name())
                && milestoneRepository.existsByProject_ProjectIdAndName(projectId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MILESTONE_NAME);
        }
        milestone.update(
                request.name(), request.startDate(), request.endDate()
        );
        return toMilestoneDto(milestone);
    }
    @Transactional
    public void deleteMilestone(Long projectId, Long milestoneId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);
        validateProjectWritable(project);

        Milestone milestone = getMilestone(projectId, milestoneId);

        milestoneRepository.delete(milestone);
    }
    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(()-> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
    private Milestone getMilestone(Long projectId, Long milestoneId) {
        return milestoneRepository.findByMilestoneIdAndProject_ProjectId(milestoneId, projectId)
                .orElseThrow(()-> new BusinessException(ErrorCode.MILESTONE_NOT_FOUND));
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
    private void validateProjectWritable(Project project) {
        if (project.getStatus() == ProjectStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_ACTIVE);
        }
    }
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
    private MilestoneDto toMilestoneDto(Milestone milestone) {
        return new MilestoneDto(
                milestone.getMilestoneId(),
                milestone.getName(),
                milestone.getStartDate(),
                milestone.getEndDate()
        );
    }
}
