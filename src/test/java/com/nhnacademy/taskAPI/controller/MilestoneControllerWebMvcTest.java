package com.nhnacademy.taskAPI.controller;

import com.nhnacademy.taskAPI.service.MilestoneService;
import com.nhnacademy.taskAPI.task.MilestoneDetailDto;
import com.nhnacademy.taskAPI.task.MilestoneDto;
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

@WebMvcTest(MileStoneController.class)
class MilestoneControllerWebMvcTest {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MilestoneService milestoneService;

    @Test
    void getMilestonesUsesMilestoneService() throws Exception {
        when(milestoneService.getMilestones(1L, "user1")).thenReturn(List.of(
                new MilestoneDto(10L, "Sprint 1", LocalDate.of(2026, 5, 15), LocalDate.of(2026, 5, 20))
        ));

        mockMvc.perform(get("/projects/{projectId}/milestones", 1L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].milestoneId").value(10))
                .andExpect(jsonPath("$[0].name").value("Sprint 1"))
                .andExpect(jsonPath("$[0].startDate").value("2026-05-15"))
                .andExpect(jsonPath("$[0].endDate").value("2026-05-20"));

        verify(milestoneService).getMilestones(1L, "user1");
    }

    @Test
    void getMilestoneDetailUsesMilestoneService() throws Exception {
        MilestoneDetailDto response = new MilestoneDetailDto(
                10L,
                "Sprint 1",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 20),
                List.of(new TaskDto(
                        20L,
                        10L,
                        "Task",
                        "Content",
                        "user1",
                        LocalDateTime.of(2026, 5, 15, 10, 0),
                        List.of()
                ))
        );

        when(milestoneService.getMilestoneDetail(1L, 10L, "user1")).thenReturn(response);

        mockMvc.perform(get("/projects/{projectId}/milestones/{milestoneId}", 1L, 10L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestoneId").value(10))
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.tasks", hasSize(1)))
                .andExpect(jsonPath("$.tasks[0].taskId").value(20));

        verify(milestoneService).getMilestoneDetail(1L, 10L, "user1");
    }

    @Test
    void createMilestoneUsesMilestoneService() throws Exception {
        MilestoneDto response = new MilestoneDto(
                10L,
                "Sprint 1",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 20)
        );

        when(milestoneService.createMilestone(eq(1L), eq("user1"), any())).thenReturn(response);

        mockMvc.perform(post("/projects/{projectId}/milestones", 1L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sprint 1",
                                  "startDate": "2026-05-15",
                                  "endDate": "2026-05-20"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.milestoneId").value(10))
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.startDate").value("2026-05-15"))
                .andExpect(jsonPath("$.endDate").value("2026-05-20"));

        verify(milestoneService).createMilestone(eq(1L), eq("user1"), any());
    }

    @Test
    void updateMilestoneUsesMilestoneService() throws Exception {
        MilestoneDto response = new MilestoneDto(
                10L,
                "Sprint 2",
                LocalDate.of(2026, 5, 21),
                LocalDate.of(2026, 5, 25)
        );

        when(milestoneService.updateMilestone(eq(1L), eq(10L), eq("user1"), any())).thenReturn(response);

        mockMvc.perform(put("/projects/{projectId}/milestones/{milestoneId}/edit", 1L, 10L)
                        .header(USER_ID_HEADER, "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sprint 2",
                                  "startDate": "2026-05-21",
                                  "endDate": "2026-05-25"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.milestoneId").value(10))
                .andExpect(jsonPath("$.name").value("Sprint 2"))
                .andExpect(jsonPath("$.startDate").value("2026-05-21"))
                .andExpect(jsonPath("$.endDate").value("2026-05-25"));

        verify(milestoneService).updateMilestone(eq(1L), eq(10L), eq("user1"), any());
    }

    @Test
    void deleteMilestoneReturnsNoContent() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/milestones/{milestoneId}/delete", 1L, 10L)
                        .header(USER_ID_HEADER, "user1"))
                .andExpect(status().isNoContent());

        verify(milestoneService).deleteMilestone(1L, 10L, "user1");
    }
}
