package ru.ilyavolodin.tasktimetrackerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.ilyavolodin.tasktimetrackerservice.config.TestSecurityConfig;
import ru.ilyavolodin.tasktimetrackerservice.controller.TimeRecordController;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTimeRecordRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TimeRecordDto;
import ru.ilyavolodin.tasktimetrackerservice.exception.ResourceNotFoundException;
import ru.ilyavolodin.tasktimetrackerservice.exception.ValidationConflictException;
import ru.ilyavolodin.tasktimetrackerservice.service.TimeRecordService;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeRecordController.class)
@DisplayName("TimeRecordController")
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class TimeRecordControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @MockitoBean
    private TimeRecordService timeRecordService;
    
    @Nested
    @DisplayName("POST /api/time-records")
    class CreateTimeRecordTests {
        @Test
        @DisplayName("должен вернуть 201 Created при успешном создании")
        void shouldReturn201WhenRecordCreated() throws Exception {
            var request = new CreateTimeRecordRequest(
                    1L, 1L,
                    LocalDateTime.of(2026, 4, 19, 9, 0),
                    LocalDateTime.of(2026, 4, 19, 10, 0),
                    "Работа"
            );
            var response = new TimeRecordDto(1L, 1L, 1L, request.startTime(), request.endTime(), "Работа");
            given(timeRecordService.createTimeRecord(request)).willReturn(response);
            mockMvc.perform(post("/api/time-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("должен вернуть 409 при нарушении бизнес-правила")
        void shouldReturn409WhenBusinessRuleViolated() throws Exception {
            var request = new CreateTimeRecordRequest(
                    1L, 1L,
                    LocalDateTime.of(2026, 4, 19, 10, 0),
                    LocalDateTime.of(2026, 4, 19, 9, 0),
                    "Работа"
            );
            doThrow(new ValidationConflictException("End time must be after start time"))
                    .when(timeRecordService).createTimeRecord(request);
            mockMvc.perform(post("/api/time-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("End time must be after start time"));
        }

        @Test
        @DisplayName("должен вернуть 404 когда задача не найдена")
        void shouldReturn404WhenTaskNotFound() throws Exception {
            var request = new CreateTimeRecordRequest(
                    1L, 999L,
                    LocalDateTime.of(2026, 4, 19, 9, 0),
                    LocalDateTime.of(2026, 4, 19, 10, 0),
                    "Работа"
            );
            doThrow(new ResourceNotFoundException("Task", 999L))
                    .when(timeRecordService).createTimeRecord(request);
            mockMvc.perform(post("/api/time-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/time-records")
    class GetRecordsTests {
        @Test
        @DisplayName("должен вернуть 200 со списком записей")
        void shouldReturn200WithRecords() throws Exception {
            Long employeeId = 1L;
            var start = LocalDateTime.of(2026, 4, 19, 0, 0);
            var end = LocalDateTime.of(2026, 4, 19, 23, 59);
            var dto = new TimeRecordDto(1L, employeeId, 1L, start, end, "Работа");
            given(timeRecordService.getRecordsByEmployeeAndPeriod(employeeId, start, end))
                    .willReturn(List.of(dto));
            mockMvc.perform(get("/api/time-records")
                            .param("employeeId", String.valueOf(employeeId))
                            .param("start", start.toString())
                            .param("end", end.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("должен вернуть 409 при некорректном периоде")
        void shouldReturn409WhenInvalidPeriod() throws Exception {
            var start = LocalDateTime.of(2026, 4, 20, 0, 0);
            var end = LocalDateTime.of(2026, 4, 19, 0, 0);
            doThrow(new ValidationConflictException("End of period cannot be before start of period"))
                    .when(timeRecordService).getRecordsByEmployeeAndPeriod(1L, start, end);
            mockMvc.perform(get("/api/time-records")
                            .param("employeeId", "1")
                            .param("start", start.toString())
                            .param("end", end.toString()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("End of period cannot be before start of period"));
        }
    }
}
