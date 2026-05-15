package com.nhnacademy.taskAPI.service;

import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Tag;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.ProjectMemberRepository;
import com.nhnacademy.taskAPI.repository.ProjectRepository;
import com.nhnacademy.taskAPI.repository.TagRepository;
import com.nhnacademy.taskAPI.task.TagCreateRequest;
import com.nhnacademy.taskAPI.task.TagDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void getTagsReturnsProjectTags() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Tag backend = tag(10L, project, "Backend");
        Tag frontend = tag(11L, project, "Frontend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.findByProject_ProjectId(1L)).thenReturn(List.of(backend, frontend));

        List<TagDto> response = tagService.getTags(1L, "user1");

        assertThat(response).hasSize(2);
        assertThat(response).extracting("name").containsExactly("Backend", "Frontend");
    }

    @Test
    void createTagCreatesTag() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.existsByProject_ProjectIdAndName(1L, "Backend")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            ReflectionTestUtils.setField(tag, "tagId", 10L);
            return tag;
        });

        TagDto response = tagService.createTag(
                1L,
                "user1",
                new TagCreateRequest("Backend")
        );

        assertThat(response.tagId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Backend");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void createTagRejectsDuplicateName() {
        Project project = project(1L, ProjectStatus.ACTIVE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.existsByProject_ProjectIdAndName(1L, "Backend")).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag(
                1L,
                "user1",
                new TagCreateRequest("Backend")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_TAG_NAME);
    }

    @Test
    void createTagRejectsTerminatedProject() {
        Project project = project(1L, ProjectStatus.TERMINATED);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag(
                1L,
                "user1",
                new TagCreateRequest("Backend")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROJECT_NOT_ACTIVE);
    }

    @Test
    void updateTagUpdatesName() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Tag tag = tag(10L, project, "Backend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.findByTagIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByProject_ProjectIdAndName(1L, "Backend Updated")).thenReturn(false);

        TagDto response = tagService.updateTag(
                1L,
                10L,
                "user1",
                new TagCreateRequest("Backend Updated")
        );

        assertThat(response.tagId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Backend Updated");
        assertThat(tag.getName()).isEqualTo("Backend Updated");
    }

    @Test
    void updateTagRejectsDuplicateName() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Tag tag = tag(10L, project, "Backend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.findByTagIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByProject_ProjectIdAndName(1L, "Frontend")).thenReturn(true);

        assertThatThrownBy(() -> tagService.updateTag(
                1L,
                10L,
                "user1",
                new TagCreateRequest("Frontend")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_TAG_NAME);
    }

    @Test
    void deleteTagDeletesTag() {
        Project project = project(1L, ProjectStatus.ACTIVE);
        Tag tag = tag(10L, project, "Backend");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProject_ProjectIdAndUserId(1L, "user1")).thenReturn(true);
        when(tagRepository.findByTagIdAndProject_ProjectId(10L, 1L)).thenReturn(Optional.of(tag));

        tagService.deleteTag(1L, 10L, "user1");

        verify(tagRepository).delete(tag);
    }

    private Project project(Long projectId, ProjectStatus status) {
        Project project = new Project("Project", status, "admin");
        ReflectionTestUtils.setField(project, "projectId", projectId);
        return project;
    }

    private Tag tag(Long tagId, Project project, String name) {
        Tag tag = new Tag(project, name);
        ReflectionTestUtils.setField(tag, "tagId", tagId);
        return tag;
    }
}