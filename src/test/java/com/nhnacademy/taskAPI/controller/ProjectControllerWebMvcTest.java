package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.ProjectService;
import com.nhnacademy.taskAPI.task.ProjectCreateRequest;
import com.nhnacademy.taskAPI.task.ProjectDetailDto;
import com.nhnacademy.taskAPI.task.ProjectDto;
import com.nhnacademy.taskAPI.task.ProjectUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void getProjectsUsesProjectService() throws Exception {
        when(projectService.getProjects("user1")).thenReturn(List.of(
                new ProjectDto(1L, "Project", "ACTIVE")
        ));

        mockMvc.perform(get("/projects")
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].name").value("Project"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(projectService).getProjects("user1");
    }

    @Test
    void getProjectDetailUsesProjectService() throws Exception {
        when(projectService.getProjectDetail(1L, "user1")).thenReturn(new ProjectDetailDto(
                1L,
                "Project",
                "ACTIVE",
                "user1",
                List.of(),
                List.of(),
                List.of()
        ));

        mockMvc.perform(get("/projects/1")
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Project"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(projectService).getProjectDetail(1L, "user1");
    }

    @Test
    void createProjectUsesProjectService() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest("Project");

        when(projectService.createProject(eq("user1"), any(ProjectCreateRequest.class)))
                .thenReturn(new ProjectDto(1L, "Project", "ACTIVE"));

        mockMvc.perform(post("/projects")
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Project"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(projectService).createProject(eq("user1"), any(ProjectCreateRequest.class));
    }

    @Test
    void updateProjectUsesProjectService() throws Exception {
        ProjectUpdateRequest request = new ProjectUpdateRequest("Updated", "DORMANT");

        when(projectService.updateProject(eq(1L), eq("user1"), any(ProjectUpdateRequest.class)))
                .thenReturn(new ProjectDto(1L, "Updated", "DORMANT"));

        mockMvc.perform(put("/projects/1/edit")
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.status").value("DORMANT"));

        verify(projectService).updateProject(eq(1L), eq("user1"), any(ProjectUpdateRequest.class));
    }

    @Test
    void closeProjectUsesProjectService() throws Exception {
        when(projectService.closeProject(1L, "user1"))
                .thenReturn(new ProjectDto(1L, "Project", "TERMINATED"));

        mockMvc.perform(post("/projects/1/close")
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Project"))
                .andExpect(jsonPath("$.status").value("TERMINATED"));

        verify(projectService).closeProject(1L, "user1");
    }
}
