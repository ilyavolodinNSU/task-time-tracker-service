package ru.ilyavolodin.tasktimetrackerservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTimeRecordRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TimeRecordDto;
import ru.ilyavolodin.tasktimetrackerservice.service.TimeRecordService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/time-records")
@RequiredArgsConstructor
@Tag(name = "Time Records", description = "API for tracking time spent on tasks")
public class TimeRecordController {
    private final TimeRecordService timeRecordService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @Operation(summary = "Log time spent on a task")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Time record created"),
        @ApiResponse(responseCode = "400", description = "Invalid time interval"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Time overlap or business rule violation")
    })
    public ResponseEntity<TimeRecordDto> createTimeRecord(
        @Valid @RequestBody CreateTimeRecordRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timeRecordService.createTimeRecord(request));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    @Operation(summary = "Get time records for an employee in a period")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Records retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid period")
    })
    public ResponseEntity<List<TimeRecordDto>> getRecords(
            @Valid @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(
            timeRecordService.getRecordsByEmployeeAndPeriod(employeeId, start, end)
        );
    }
}