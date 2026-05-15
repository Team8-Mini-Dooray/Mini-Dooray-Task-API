package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TaskService;
import com.nhnacademy.taskAPI.task.CommentDto;
import com.nhnacademy.taskAPI.task.MilestoneDto;
import com.nhnacademy.taskAPI.task.TagDto;
import com.nhnacademy.taskAPI.task.TaskCreateRequest;
import com.nhnacademy.taskAPI.task.TaskDetailDto;
import com.nhnacademy.taskAPI.task.TaskDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTaskBindsOptionalParameters() throws Exception {
        TaskDto response = new TaskDto(
                20L,
                10L,
                "Task",
                "Content",
                "user1",
                LocalDateTime.of(2026, 5, 15, 10, 0),
                List.of(new TagDto(100L, "Backend"))
        );

        when(taskService.createTask(
                eq(1L),
                any(TaskCreateRequest.class),
                eq(10L),
                eq("Sprint 1"),
                eq(LocalDate.of(2026, 5, 15)),
                eq(LocalDate.of(2026, 5, 20)),
                eq(List.of(100L, 101L)),
                eq("Frontend"),
                eq("user1")
        )).thenReturn(response);

        mockMvc.perform(post("/projects/{projectId}/tasks", 1L)
                        .header(USER_ID_HEADER, "user1")
                        .param("milestoneId", "10")
                        .param("newMilestoneName", "Sprint 1")
                        .param("newMilestoneStartDate", "2026-05-15")
                        .param("newMilestoneEndDate", "2026-05-20")
                        .param("tagIds", "100", "101")
                        .param("newTagName", "Frontend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 1,
                                  "title": "Task",
                                  "content": "Content",
                                  "writerId": "ignored"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(20))
                .andExpect(jsonPath("$.milestoneId").value(10))
                .andExpect(jsonPath("$.writerId").value("user1"))
                .andExpect(jsonPath("$.tags[0].name").value("Backend"));

        verify(taskService).createTask(
                eq(1L),
                any(TaskCreateRequest.class),
                eq(10L),
                eq("Sprint 1"),
                eq(LocalDate.of(2026, 5, 15)),
                eq(LocalDate.of(2026, 5, 20)),
                eq(List.of(100L, 101L)),
                eq("Frontend"),
                eq("user1")
        );
    }

    @Test
    void getTaskReturnsMilestoneTagsAndComments() throws Exception {
        TaskDetailDto response = new TaskDetailDto(
                20L,
                "Task",
                "Content",
                "user1",
                LocalDateTime.of(2026, 5, 15, 10, 0),
                new MilestoneDto(10L, "Sprint 1", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 20)),
                List.of(new TagDto(100L, "Backend")),
                List.of(new CommentDto(30L, "user2", "Comment", LocalDateTime.of(2026, 5, 15, 11, 0)))
        );

        when(taskService.getTask(1L, 20L, "user1")).thenReturn(response);

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}", 1L, 20L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(20))
                .andExpect(jsonPath("$.milestone.name").value("Sprint 1"))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].writerId").value("user2"));
    }

    @Test
    void deleteTaskReturnsNoContent() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/tasks/{taskId}/delete", 1L, 20L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L, 20L, "user1");
    }

}
