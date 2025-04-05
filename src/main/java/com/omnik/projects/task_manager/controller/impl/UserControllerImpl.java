package com.omnik.projects.task_manager.controller.impl;

import com.omnik.projects.task_manager.controller.UserController;
import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserControllerImpl implements UserController {

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<ApiResponseDTO<?>> addUser(String requesterUsername, UserRequestDTO userCreationRequest) {
        return ResponseEntity.ok(userService.addUser(requesterUsername,userCreationRequest));
    }

}
