package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.enums.TaskStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public interface TaskController {

    @PostMapping("/create-new/{requester-username}")
    ResponseEntity<ApiResponseDTO<?>> createTask(@PathVariable("requester-username") String requesterUsername,@RequestParam("scheduleTask")  boolean scheduleTask,
                                                 @RequestParam("bufferTask") boolean bufferTask,@RequestBody TaskRequestDTO taskCreationRequest);

    @PostMapping("/assign-new/{requester-username}/{assignee-username}")
    ResponseEntity<ApiResponseDTO<?>> assignTask(@PathVariable("requester-username") String requesterUsername, @PathVariable("assignee-username") String assigneeUsername,
                                                 @RequestParam("scheduleTask")  boolean scheduleTask, @RequestParam("bufferTask") boolean bufferTask,@RequestBody TaskRequestDTO taskCreationRequest);

    @PostMapping("/schedule/{task-name}")
    ResponseEntity<ApiResponseDTO<?>> scheduleTask(@PathVariable("task-name") String taskName);

    @PostMapping("/buffer/{task-name}")
    ResponseEntity<ApiResponseDTO<?>> bufferTask(@PathVariable("task-name") String taskName);

    @PatchMapping("/process/{requester-username}/{task-name}/{status}")
    ResponseEntity<ApiResponseDTO<?>> processTask(@PathVariable("requester-username")String requesterUsername, @PathVariable("task-name")String taskName, @PathVariable("status")TaskStatus status);

    @DeleteMapping("/delete/{requester-username}/{task-name}")
    ResponseEntity<ApiResponseDTO<?>> deleteTask(@PathVariable("requester-username") String requesterUsername,@PathVariable("task-name") String taskName);

    @PatchMapping("/complete/{requester-username}/{task-name}")
    ResponseEntity<ApiResponseDTO<?>> markCompleted(@PathVariable("requester-username") String requesterUsername,@PathVariable("task-name") String taskName);

}
