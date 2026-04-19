package ru.ilyavolodin.tasktimetrackerservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.ilyavolodin.tasktimetrackerservice.dto.CreateTaskRequest;
import ru.ilyavolodin.tasktimetrackerservice.dto.TaskDto;
import ru.ilyavolodin.tasktimetrackerservice.dto.UpdateStatusRequest;
import ru.ilyavolodin.tasktimetrackerservice.service.TaskService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Managing tasks api")
public class TaskController {

    private final TaskService taskService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @Operation(summary = "Create new task")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<TaskDto> createTask(
        @Valid @RequestBody CreateTaskRequest request
    ) { 
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Status updated"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public ResponseEntity<Void> updateStatus(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateStatusRequest request
    ) {
        taskService.updateTaskStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}
