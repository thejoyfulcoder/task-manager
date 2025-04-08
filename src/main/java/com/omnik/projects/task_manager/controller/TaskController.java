package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
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

    @PostMapping("/schedule/{taskname}")
    ResponseEntity<ApiResponseDTO<?>> scheduleTask(@PathVariable("taskname") String taskName);

    @PostMapping("/buffer/{taskname}")
    ResponseEntity<ApiResponseDTO<?>> bufferTask(@PathVariable("taskname") String taskName);@PostMapping("/buffer/{taskname}")

    @DeleteMapping("/delete/{requester-username}/{taskname}")
    ResponseEntity<ApiResponseDTO<?>> deleteTask(@PathVariable("requester-username") String requesterUsername,@PathVariable("taskname") String taskName);

}
