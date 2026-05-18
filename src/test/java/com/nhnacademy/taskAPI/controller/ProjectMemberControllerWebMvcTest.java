package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.ProjectMemberService;
import com.nhnacademy.taskAPI.task.ProjectMemberDto;
import com.nhnacademy.taskAPI.task.ProjectMemberRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectMemberController.class)
class ProjectMemberControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @Test
    void getMembersUsesProjectMemberService() throws Exception {
        when(projectMemberService.getMembers(1L, "user1")).thenReturn(List.of(
                new ProjectMemberDto("user1"),
                new ProjectMemberDto("user2")
        ));

        mockMvc.perform(get("/projects/1/members")
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value("user1"))
                .andExpect(jsonPath("$[1].userId").value("user2"));

        verify(projectMemberService).getMembers(1L, "user1");
    }

    @Test
    void addMemberUsesProjectMemberService() throws Exception {
        ProjectMemberRequest request = new ProjectMemberRequest("user2");

        when(projectMemberService.addMember(eq(1L), eq("admin"), any(ProjectMemberRequest.class)))
                .thenReturn(new ProjectMemberDto("user2"));

        mockMvc.perform(post("/projects/1/members")
                        .header(USER_ID_HEADER, "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user2"));

        verify(projectMemberService).addMember(eq(1L), eq("admin"), any(ProjectMemberRequest.class));
    }

    @Test
    void removeMemberReturnsNoContent() throws Exception {
        mockMvc.perform(post("/projects/1/members/user2/delete")
                        .header(USER_ID_HEADER, "admin"))
                .andExpect(status().isNoContent());

        verify(projectMemberService).removeMember(1L, "admin", "user2");
    }
}
