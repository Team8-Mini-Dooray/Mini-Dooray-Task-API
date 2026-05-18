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
class ProjectMemberServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    @Test
    void getMembersReturnsProjectMembers() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");
        ProjectMember admin = projectMember(100L, project, "admin");
        ProjectMember user1 = projectMember(101L, project, "user1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "admin")).thenReturn(true);
        when(projectMemberRepository.findByProject_ProjectId(1L)).thenReturn(List.of(admin, user1));

        List<ProjectMemberDto> response = projectMemberService.getMembers(1L, "admin");

        assertThat(response).hasSize(2);
        assertThat(response).extracting("userId").containsExactly("admin", "user1");
    }

    @Test
    void addMemberAddsProjectMember() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> {
            ProjectMember member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "projectMemberId", 100L);
            return member;
        });

        ProjectMemberDto response = projectMemberService.addMember(
                1L,
                "admin",
                new ProjectMemberRequest("user1")
        );

        assertThat(response.userId()).isEqualTo("user1");
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void addMemberRejectsNonAdmin() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");
        ProjectMemberRequest request = new ProjectMemberRequest("user2");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectMemberService.addMember(1L, "user1", request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PROJECT_ADMIN);
    }

    @Test
    void addMemberRejectsDuplicateMember() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");
        ProjectMemberRequest request = new ProjectMemberRequest("user2");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user2"))
                .thenReturn(true);

        assertThatThrownBy(() -> projectMemberService.addMember(1L, "admin", request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_PROJECT_MEMBER);
    }

    @Test
    void addMemberRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED, "admin");
        ProjectMemberRequest request = new ProjectMemberRequest("user2");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectMemberService.addMember(1L, "admin", request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_ACTIVE);
    }

    @Test
    void removeMemberDeletesProjectMember() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");
        ProjectMember member = projectMember(100L, project, "user1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProject_ProjectIdAndUserId(1L, "user1"))
                .thenReturn(Optional.of(member));

        projectMemberService.removeMember(1L, "admin", "user1");

        verify(projectMemberRepository).delete(member);
    }

    @Test
    void removeMemberRejectsAdminRemoval() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectMemberService.removeMember(
                1L,
                "admin",
                "admin"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADMIN_MEMBER_CANNOT_BE_REMOVED);
    }

    @Test
    void removeMemberRejectsMissingMember() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProject_ProjectIdAndUserId(1L, "user1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.removeMember(
                1L,
                "admin",
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_MEMBER_NOT_FOUND);
    }

    @Test
    void removeMemberRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectMemberService.removeMember(
                1L,
                "admin",
                "user1"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_ACTIVE);
    }
    @Test
    void getMembersRejectsNullUserId() {
        assertThatThrownBy(() -> projectMemberService.getMembers(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MISSING_USER_ID);
    }
    @Test
    void getMembersRejectsBlankUserId() {
        assertThatThrownBy(() -> projectMemberService.getMembers(1L, " "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MISSING_USER_ID);
    }
    @Test
    void getMembersRejectsNonMember() {
        Project project = project(1L, ProjectStatus.ACTIVE, "admin");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1"))
                .thenReturn(false);

        assertThatThrownBy(() -> projectMemberService.getMembers(1L, "user1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_PROJECT_MEMBER);
    }
    @Test
    void getMembersRejectsNotFoundProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectMemberService.getMembers(1L, "user1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    private Project project(Long projectId, ProjectStatus status, String adminId) {
        Project project = new Project("Project", status, adminId);
        ReflectionTestUtils.setField(project, "projectId", projectId);
        return project;
    }

    private ProjectMember projectMember(Long projectMemberId, Project project, String userId) {
        ProjectMember projectMember = new ProjectMember(project, userId);
        ReflectionTestUtils.setField(projectMember, "projectMemberId", projectMemberId);
        return projectMember;
    }
}