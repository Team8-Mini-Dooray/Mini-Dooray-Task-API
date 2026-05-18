package com.nhnacademy.taskAPI.service;


import com.nhnacademy.taskAPI.entity.Comment;
import com.nhnacademy.taskAPI.entity.Milestone;
import com.nhnacademy.taskAPI.entity.Project;
import com.nhnacademy.taskAPI.entity.ProjectStatus;
import com.nhnacademy.taskAPI.entity.Tag;
import com.nhnacademy.taskAPI.entity.Task;
import com.nhnacademy.taskAPI.entity.TaskTag;
import com.nhnacademy.taskAPI.exception.BusinessException;
import com.nhnacademy.taskAPI.exception.ErrorCode;
import com.nhnacademy.taskAPI.repository.*;
import com.nhnacademy.taskAPI.task.MilestoneCreateRequest;
import com.nhnacademy.taskAPI.task.CommentDto;
import com.nhnacademy.taskAPI.task.MilestoneDto;
import com.nhnacademy.taskAPI.task.TagCreateRequest;
import com.nhnacademy.taskAPI.task.TagDto;
import com.nhnacademy.taskAPI.task.TaskCreateRequest;
import com.nhnacademy.taskAPI.task.TaskDetailDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import com.nhnacademy.taskAPI.task.TaskMilestoneRequest;
import com.nhnacademy.taskAPI.task.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskTagRepository taskTagRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final MilestoneRepository milestoneRepository;
    private final CommentRepository commentRepository;
    private final TagService tagService;
    private final MilestoneService milestoneService;

    public List<TaskDto> getTasks(Long projectId, Long tagId, String userId) {
        getProject(projectId);
        validateProjectMember(projectId, userId);

        if (tagId == null) {
            return toTaskDtos(taskRepository.findByProject_ProjectId(projectId));
        }

        getTagsInProject(projectId, List.of(tagId));
        List<Task> tasks = taskTagRepository.findByTask_Project_ProjectIdAndTag_TagId(projectId, tagId)
                .stream()
                .map(TaskTag::getTask)
                .toList();
        return toTaskDtos(tasks);
    }

    public TaskDetailDto getTask(Long projectId, Long taskId, String userId) {
        validateProjectMember(projectId, userId);

        Task task = getTaskInProject(projectId, taskId);
        List<CommentDto> comments = commentRepository.findByTask_TaskId(taskId)
                .stream()
                .map(this::toCommentDto)
                .toList();

        return toTaskDetailDto(task, comments);
    }

    @Transactional
    public TaskDto createTask(
            Long projectId,
            TaskCreateRequest request,
            Long milestoneId,
            String newMilestoneName,
            LocalDate newMilestoneStartDate,
            LocalDate newMilestoneEndDate,
            List<Long> tagIds,
            String newTagName,
            String userId
    ) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);

        Milestone milestone = resolveMilestone(
                project,
                milestoneId,
                newMilestoneName,
                newMilestoneStartDate,
                newMilestoneEndDate,
                userId
        );
        Task task = taskRepository.save(new Task(project, milestone, request.title(), request.content(), userId));

        List<Tag> tags = resolveTags(project, tagIds, newTagName, userId);
        if (!tags.isEmpty()) {
            taskTagRepository.saveAll(tags.stream()
                    .map(tag -> new TaskTag(task, tag))
                    .toList());
        }

        return toTaskDto(task);
    }

    @Transactional
    public TaskDto updateTask(Long projectId, Long taskId, TaskUpdateRequest request, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);

        Task task = getTaskInProject(projectId, taskId);
        task.update(request.title(), request.content(), task.getWriterId());

        return toTaskDto(task);
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);

        Task task = getTaskInProject(projectId, taskId);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskDto updateTaskMilestone(Long projectId, Long taskId, TaskMilestoneRequest request, String userId) {
        Project project = getProject(projectId);
        validateProjectWritable(project);
        validateProjectMember(projectId, userId);

        Task task = getTaskInProject(projectId, taskId);
        if (request.milestoneId() == null) {
            task.removeMilestone();
            return toTaskDto(task);
        }

        Milestone milestone = getMilestoneInProject(projectId, request.milestoneId());
        task.updateMilestone(milestone);

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

    private Milestone getMilestoneInProject(Long projectId, Long milestoneId) {
        return milestoneRepository.findByMilestoneIdAndProject_ProjectId(milestoneId, projectId)
                .orElseThrow(() -> {
                    if (milestoneRepository.existsById(milestoneId)) {
                        return new BusinessException(ErrorCode.MILESTONE_NOT_IN_PROJECT);
                    }
                    return new BusinessException(ErrorCode.MILESTONE_NOT_FOUND);
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

    private Milestone resolveMilestone(
            Project project,
            Long milestoneId,
            String newMilestoneName,
            LocalDate newMilestoneStartDate,
            LocalDate newMilestoneEndDate,
            String userId
    ) {
        if (hasText(newMilestoneName)) {
            MilestoneDto milestone = milestoneService.createMilestone(
                    project.getProjectId(),
                    userId,
                    new MilestoneCreateRequest(
                            newMilestoneName,
                            newMilestoneStartDate,
                            newMilestoneEndDate
                    )
            );
            return getMilestoneInProject(project.getProjectId(), milestone.milestoneId());
        }

        if (milestoneId == null) {
            return null;
        }

        return getMilestoneInProject(project.getProjectId(), milestoneId);
    }

    private List<Tag> resolveTags(Project project, List<Long> tagIds, String newTagName, String userId) {
        Set<Long> resolvedTagIds = new HashSet<>();
        if (tagIds != null) {
            resolvedTagIds.addAll(tagIds);
        }

        if (hasText(newTagName)) {
            TagDto tag = tagService.createTag(
                    project.getProjectId(),
                    userId,
                    new TagCreateRequest(newTagName)
            );
            resolvedTagIds.add(tag.tagId());
        }

        if (resolvedTagIds.isEmpty()) {
            return List.of();
        }

        return getTagsInProject(project.getProjectId(), List.copyOf(resolvedTagIds));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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

    private List<TaskDto> toTaskDtos(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        List<Long> taskIds = tasks.stream()
                .map(Task::getTaskId)
                .toList();
        Map<Long, List<TagDto>> tagsByTaskId = taskTagRepository.findByTask_TaskIdIn(taskIds)
                .stream()
                .collect(groupingBy(
                        taskTag -> taskTag.getTask().getTaskId(),
                        mapping(taskTag -> toTagDto(taskTag.getTag()), toList())
                ));

        return tasks.stream()
                .map(task -> toTaskDto(task, tagsByTaskId.getOrDefault(task.getTaskId(), List.of())))
                .toList();
    }

    private TaskDto toTaskDto(Task task, List<TagDto> tags) {
        return new TaskDto(
                task.getTaskId(),
                task.getMilestone() == null ? null : task.getMilestone().getMilestoneId(),
                task.getTitle(),
                task.getContent(),
                task.getWriterId(),
                task.getCreatedAt(),
                tags
        );
    }

    private TaskDetailDto toTaskDetailDto(Task task, List<CommentDto> comments) {
        return new TaskDetailDto(
                task.getTaskId(),
                task.getTitle(),
                task.getContent(),
                task.getWriterId(),
                task.getCreatedAt(),
                toMilestoneDto(task.getMilestone()),
                getTaskTags(task.getTaskId()),
                comments
        );
    }

    private List<TagDto> getTaskTags(Long taskId) {
        return taskTagRepository.findByTask_TaskId(taskId)
                .stream()
                .map(TaskTag::getTag)
                .map(this::toTagDto)
                .toList();
    }

    private MilestoneDto toMilestoneDto(Milestone milestone) {
        if (milestone == null) {
            return null;
        }

        return new MilestoneDto(
                milestone.getMilestoneId(),
                milestone.getName(),
                milestone.getStartDate(),
                milestone.getEndDate()
        );
    }

    private TagDto toTagDto(Tag tag) {
        return new TagDto(
                tag.getTagId(),
                tag.getName()
        );
    }

    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getCommentId(),
                comment.getWriterId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
