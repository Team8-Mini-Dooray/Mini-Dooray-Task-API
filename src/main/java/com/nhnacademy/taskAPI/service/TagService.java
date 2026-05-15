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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TagRepository tagRepository;

    public List<TagDto> getTags(Long projectId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(project.getProjectId(), userId);

        return tagRepository.findByProject_ProjectId(projectId)
                .stream()
                .map(this::toTagDto)
                .toList();
    }

    @Transactional
    public TagDto createTag(Long projectId, String userId, TagCreateRequest request) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);
        validateProjectWritable(project);

        if (tagRepository.existsByProject_ProjectIdAndName(projectId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TAG_NAME);
        }
        Tag tag = new Tag(project, request.name());
        Tag savedTag = tagRepository.save(tag);
        return toTagDto(savedTag);
    }

    @Transactional
    public TagDto updateTag(Long projectId, Long tagId, String userId, TagCreateRequest request) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);
        validateProjectWritable(project);

        Tag tag = getTag(projectId, tagId);

        if (!tag.getName().equals(request.name())
                && tagRepository.existsByProject_ProjectIdAndName(projectId, request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TAG_NAME);
        }

        tag.updateName(request.name());
        return toTagDto(tag);
    }

    @Transactional
    public void deleteTag(Long projectId, Long tagId, String userId) {
        validateUserId(userId);

        Project project = getProject(projectId);
        validateProjectMember(projectId, userId);
        validateProjectWritable(project);

        Tag tag = getTag(projectId, tagId);

        tagRepository.delete(tag);
    }
    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(()-> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
    private Tag getTag(Long projectId, Long tagId) {
        return tagRepository.findByTagIdAndProject_ProjectId(tagId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
    }
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_USER_ID);
        }
    }
    private void validateProjectMember(Long projectId, String userId) {
        if (!projectMemberRepository.existsByProject_ProjectIdAndUserId(projectId, userId)){
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }
    private void validateProjectWritable(Project project) {
        if (project.getStatus() == ProjectStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_ACTIVE);
        }
    }
    private TagDto toTagDto(Tag tag) {
        return new TagDto(
                tag.getTagId(),
                tag.getName()
        );
    }
}

