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
import com.nhnacademy.taskAPI.task.TagDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskTagRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskTagService {

    private final TaskTagRepository taskTagRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TagRepository tagRepository;

    @Transactional
    public TaskDto updateTaskTags(Long projectId, Long taskId, TaskTagRequest request, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);

        Task task = getTaskInProject(projectId, taskId);
        List<Long> tagIds = request.tagIds();
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ErrorCode.TASK_TAG_REQUIRED);
        }

        List<Tag> tags = getTagsInProject(projectId, tagIds);
        taskTagRepository.deleteAllByTask_TaskId(taskId);
        taskTagRepository.flush();
        taskTagRepository.saveAll(tags.stream()
                .map(tag -> new TaskTag(task, tag))
                .toList());

        return toTaskDto(task);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
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

    private Task getTaskInProject(Long projectId, Long taskId) {
        return taskRepository.findByTaskIdAndProject_ProjectId(taskId, projectId)
                .orElseThrow(() -> {
                    if (taskRepository.existsById(taskId)) {
                        return new BusinessException(ErrorCode.TASK_NOT_IN_PROJECT);
                    }
                    return new BusinessException(ErrorCode.TASK_NOT_FOUND);
                });
    }

    private List<Tag> getTagsInProject(Long projectId, List<Long> tagIds) {
        Set<Long> requestedTagIds = new HashSet<>(tagIds);
        List<Tag> tags = tagRepository.findAllByTagIdIn(requestedTagIds);

        if (tags.size() != requestedTagIds.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        boolean hasOtherProjectTag = tags.stream()
                .anyMatch(tag -> !tag.getProject().getProjectId().equals(projectId));
        if (hasOtherProjectTag) {
            throw new BusinessException(ErrorCode.TAG_NOT_IN_PROJECT);
        }

        return tags;
    }

    private TaskDto toTaskDto(Task task) {
        return new TaskDto(
                task.getTaskId(),
                task.getMilestone() == null ? null : task.getMilestone().getMilestoneId(),
                task.getTitle(),
                task.getContent(),
                task.getWriterId(),
                task.getCreatedAt(),
                getTaskTags(task.getTaskId())
        );
    }

    private List<TagDto> getTaskTags(Long taskId) {
        return taskTagRepository.findByTask_TaskId(taskId)
                .stream()
                .map(TaskTag::getTag)
                .map(this::toTagDto)
                .toList();
    }

    private TagDto toTagDto(Tag tag) {
        return new TagDto(
                tag.getTagId(),
                tag.getName()
        );
    }
}
