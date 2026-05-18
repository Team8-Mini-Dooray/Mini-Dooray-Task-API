package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TaskTagService;
import com.nhnacademy.taskAPI.task.TagDto;
import com.nhnacademy.taskAPI.task.TaskDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskTagController.class)
class TaskTagControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskTagService taskTagService;

    @Test
    void updateTaskTagsUsesTaskTagService() throws Exception {
        TaskDto response = new TaskDto(
                20L,
                10L,
                "Task",
                "Content",
                "user1",
                LocalDateTime.of(2026, 5, 15, 10, 0),
                List.of(new TagDto(100L, "Backend"), new TagDto(101L, "Urgent"))
        );

        when(taskTagService.updateTaskTags(eq(1L), eq(20L), any(), eq("user1"))).thenReturn(response);

        mockMvc.perform(post("/projects/{projectId}/tasks/{taskId}/tags", 1L, 20L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tagIds": [100, 101]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(20))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0].name").value("Backend"));

        verify(taskTagService).updateTaskTags(eq(1L), eq(20L), any(), eq("user1"));
    }
}
