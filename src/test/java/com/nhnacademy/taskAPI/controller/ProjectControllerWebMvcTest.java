package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.ProjectService;
import com.nhnacademy.taskAPI.task.MilestoneDto;
import com.nhnacademy.taskAPI.task.ProjectDetailDto;
import com.nhnacademy.taskAPI.task.ProjectDto;
import com.nhnacademy.taskAPI.task.ProjectMemberDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

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
        ProjectDetailDto response = new ProjectDetailDto(
                1L,
                "Project",
                "ACTIVE",
                "user1",
                List.of(new ProjectMemberDto("user1")),
                List.of(new TaskDto(
                        20L,
                        10L,
                        "Task",
                        "Content",
                        "user1",
                        LocalDateTime.of(2026, 5, 15, 10, 0),
                        List.of()
                )),
                List.of(new MilestoneDto(10L, "Sprint 1", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 20)))
        );

        when(projectService.getProjectDetail(1L, "user1")).thenReturn(response);

        mockMvc.perform(get("/projects/{projectId}", 1L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.adminId").value("user1"))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.tasks[0].title").value("Task"))
                .andExpect(jsonPath("$.milestones[0].name").value("Sprint 1"));

        verify(projectService).getProjectDetail(1L, "user1");
    }

    @Test
    void createProjectUsesProjectService() throws Exception {
        ProjectDto response = new ProjectDto(1L, "Project", "ACTIVE");

        when(projectService.createProject(eq("user1"), any())).thenReturn(response);

        mockMvc.perform(post("/projects")
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Project"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Project"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(projectService).createProject(eq("user1"), any());
    }

    @Test
    void updateProjectUsesProjectService() throws Exception {
        ProjectDto response = new ProjectDto(1L, "Updated", "DORMANT");

        when(projectService.updateProject(eq(1L), eq("user1"), any())).thenReturn(response);

        mockMvc.perform(put("/projects/{projectId}/edit", 1L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated",
                                  "status": "DORMANT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.status").value("DORMANT"));

        verify(projectService).updateProject(eq(1L), eq("user1"), any());
    }

    @Test
    void closeProjectUsesProjectService() throws Exception {
        ProjectDto response = new ProjectDto(1L, "Project", "TERMINATED");

        when(projectService.closeProject(1L, "user1")).thenReturn(response);

        mockMvc.perform(post("/projects/{projectId}/close", 1L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.status").value("TERMINATED"));

        verify(projectService).closeProject(1L, "user1");
    }
}
