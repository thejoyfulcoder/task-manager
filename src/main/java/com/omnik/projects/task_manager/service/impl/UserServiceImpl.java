package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.exceptions.UserNotFoundException;
import com.omnik.projects.task_manager.service.UserService;
import com.omnik.projects.task_manager.storage.DataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserServiceImpl implements UserService {

    private final DataStore dataStore;

    @Override
    public ApiResponseDTO<?> addUser(String requesterUsername, UserRequestDTO userCreationRequest) {
        try {
          User userFromDataStore = dataStore.getUsers().stream().filter(user -> user.getUsername().equals(requesterUsername)).findFirst().orElse(null);
          if(userFromDataStore==null){
              throw new UserNotFoundException("No user exists for the requester User: "+requesterUsername);
          }
            //TODO : check if requester User has the permission to create user and proceed to user creation
        }catch (Exception e){
             return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST,e.getMessage(),true);
        }
        return null;
    }



    @Override
    public ApiResponseDTO<?> createTask(String requesterUsername, TaskRequestDTO taskCreationRequest) {
        try {

        }catch (Exception e){

        }
        return null;
    }
}
