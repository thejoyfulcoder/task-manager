package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public interface UserController {

    @PostMapping("/addUser/{requester-username}")
    ResponseEntity<ApiResponseDTO<?>> addUser(@PathVariable("requester-username") String requesterUsername,@RequestBody UserRequestDTO userCreationRequest);

}
