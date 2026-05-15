package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.CommentService;
import com.nhnacademy.taskAPI.task.CommentDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void getCommentsUsesCommentService() throws Exception {
        when(commentService.getComments(1L, 20L, "user1")).thenReturn(List.of(
                new CommentDto(30L, "user1", "Comment", LocalDateTime.of(2026, 5, 15, 11, 0))
        ));

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}/comments", 1L, 20L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].commentId").value(30))
                .andExpect(jsonPath("$[0].content").value("Comment"));

        verify(commentService).getComments(1L, 20L, "user1");
    }

    @Test
    void createCommentUsesCommentService() throws Exception {
        CommentDto response = new CommentDto(
                30L,
                "user1",
                "Comment",
                LocalDateTime.of(2026, 5, 15, 11, 0)
        );

        when(commentService.createComment(eq(1L), eq(20L), any(), eq("user1"))).thenReturn(response);

        mockMvc.perform(post("/projects/{projectId}/tasks/{taskId}/comments", 1L, 20L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Comment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(30))
                .andExpect(jsonPath("$.writerId").value("user1"))
                .andExpect(jsonPath("$.content").value("Comment"));

        verify(commentService).createComment(eq(1L), eq(20L), any(), eq("user1"));
    }

    @Test
    void updateCommentUsesCommentService() throws Exception {
        CommentDto response = new CommentDto(
                30L,
                "user1",
                "Updated",
                LocalDateTime.of(2026, 5, 15, 11, 0)
        );

        when(commentService.updateComment(eq(1L), eq(20L), eq(30L), any(), eq("user1"))).thenReturn(response);

        mockMvc.perform(put("/projects/{projectId}/tasks/{taskId}/comments/{commentId}/edit", 1L, 20L, 30L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(30))
                .andExpect(jsonPath("$.content").value("Updated"));

        verify(commentService).updateComment(eq(1L), eq(20L), eq(30L), any(), eq("user1"));
    }

    @Test
    void deleteCommentReturnsNoContent() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/tasks/{taskId}/comments/{commentId}/delete", 1L, 20L, 30L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L, 20L, 30L, "user1");
    }
}
