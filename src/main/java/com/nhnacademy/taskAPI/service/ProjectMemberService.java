package com.nhnacademy.taskAPI.service;


import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectMember;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.task.ProjectMemberDto;
import com.nhnacademy.taskAPI.task.ProjectMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<ProjectMemberDto> getMembers(Long projectId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(project.getProjectId(), userId);

        return projectMemberRepository.findByProject_ProjectId(projectId)
                .stream()
                .map(member -> new ProjectMemberDto(member.getUserId()))
                .toList();
    }

    @Transactional
    public ProjectMemberDto addMember(Long projectId, String userId, ProjectMemberRequest request) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectAdmin(project, userId);
        validateProjectWritable(project);

        if (projectMemberRepository.existsByProject_ProjectIdAndUserId(projectId, request.userId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROJECT_MEMBER);
        }
        ProjectMember projectMember = new ProjectMember(project, request.userId());
        ProjectMember savedMember = projectMemberRepository.save(projectMember);

        return new ProjectMemberDto(savedMember.getUserId());
    }

    @Transactional
    public void removeMember(Long projectId, String userId, String targetUserId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectAdmin(project, userId);
        validateProjectWritable(project);

        if (project.getAdminId().equals(targetUserId)) {
            throw new BusinessException(ErrorCode.ADMIN_MEMBER_CANNOT_BE_REMOVED);
        }
        ProjectMember projectMember = projectMemberRepository
                .findByProject_ProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
        projectMemberRepository.delete(projectMember);
    }
    private Project getProject (Long projectId){
        return projectRepository.findById(projectId).orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
    private void validateUserId(String userId){
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
        if(project.getStatus() == ProjectStatus.TERMINATED){
            throw new BusinessException(ErrorCode.PROJECT_NOT_ACTIVE);
        }
    }
}
