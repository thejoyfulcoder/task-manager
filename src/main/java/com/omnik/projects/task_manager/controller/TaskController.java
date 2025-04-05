package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public interface TaskController {

    @PostMapping("/create-new/{requester-username}")
    ResponseEntity<ApiResponseDTO<?>> createTask(@PathVariable("requester-username") String requesterUsername, @RequestBody TaskRequestDTO taskCreationRequest);

    @PostMapping("/assign-new/{requester-username}/{assignee-username}")
    ResponseEntity<ApiResponseDTO<?>> createTask(@PathVariable("requester-username") String requesterUsername, @PathVariable("assignee-username") String assigneeUsername, @RequestBody TaskRequestDTO taskCreationRequest);

}
