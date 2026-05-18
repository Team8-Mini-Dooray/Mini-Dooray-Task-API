package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.TagService;
import com.nhnacademy.taskAPI.task.TagDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
class TagControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @Test
    void getTagsUsesTagService() throws Exception {
        when(tagService.getTags(1L, "user1")).thenReturn(List.of(
                new TagDto(100L, "Backend")
        ));

        mockMvc.perform(get("/projects/{projectId}/tags", 1L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tagId").value(100))
                .andExpect(jsonPath("$[0].name").value("Backend"));

        verify(tagService).getTags(1L, "user1");
    }

    @Test
    void createTagUsesTagService() throws Exception {
        TagDto response = new TagDto(100L, "Backend");

        when(tagService.createTag(eq(1L), eq("user1"), any())).thenReturn(response);

        mockMvc.perform(post("/projects/{projectId}/tags", 1L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Backend"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tagId").value(100))
                .andExpect(jsonPath("$.name").value("Backend"));

        verify(tagService).createTag(eq(1L), eq("user1"), any());
    }

    @Test
    void updateTagUsesTagService() throws Exception {
        TagDto response = new TagDto(100L, "Frontend");

        when(tagService.updateTag(eq(1L), eq(100L), eq("user1"), any())).thenReturn(response);

        mockMvc.perform(put("/projects/{projectId}/tags/{tagId}/edit", 1L, 100L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Frontend"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagId").value(100))
                .andExpect(jsonPath("$.name").value("Frontend"));

        verify(tagService).updateTag(eq(1L), eq(100L), eq("user1"), any());
    }

    @Test
    void deleteTagReturnsNoContent() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/tags/{tagId}/delete", 1L, 100L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isNoContent());

        verify(tagService).deleteTag(1L, 100L, "user1");
    }
}
