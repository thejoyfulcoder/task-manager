package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.exceptions.IllegalOperationException;
import com.omnik.projects.task_manager.exceptions.PermissionDenialException;
import com.omnik.projects.task_manager.exceptions.TaskNotFoundException;
import com.omnik.projects.task_manager.exceptions.UserNotFoundException;
import com.omnik.projects.task_manager.service.TaskService;
import com.omnik.projects.task_manager.service.UserService;
import com.omnik.projects.task_manager.storage.DataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskServiceImpl implements TaskService {

    private final UserService userService;
    private final DataStore dataStore;

    public TaskServiceImpl(UserService userService, DataStore dataStore) {
        this.userService = userService;
        this.dataStore = dataStore;
    }

    @Override
    public ApiResponseDTO<?> createTask(String requesterUsername, boolean scheduleTask,boolean bufferTask, TaskRequestDTO taskCreationRequest) {
        try {
            User userFromDataStore = userService.validateUser(requesterUsername, Permission.Create_Task);
            Task task = new Task(taskCreationRequest.getName(),taskCreationRequest.getDescription(),taskCreationRequest.getPriority(),
                    taskCreationRequest.getStatus(),taskCreationRequest.getCategory(),taskCreationRequest.getDeadline(),userFromDataStore);
            dataStore.addNewUserTask(task);
            if(scheduleTask)dataStore.scheduleTask(task);
            return new ApiResponseDTO<>(HttpStatus.OK,"User task added successfully",false);
        }catch (PermissionDenialException | IllegalOperationException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }


    @Override
    public ApiResponseDTO<?> assignTask(String requesterUsername, String assigneeUsername, boolean scheduleTask, boolean bufferTask, TaskRequestDTO taskCreationRequest) {
        try {
            userService.validateUser(requesterUsername,Permission.Create_Task);
            User assigneeUser= userService.validateUser(assigneeUsername,null);
            Task task = new Task(taskCreationRequest.getName(),taskCreationRequest.getDescription(),taskCreationRequest.getPriority(),
                    taskCreationRequest.getStatus(),taskCreationRequest.getCategory(),taskCreationRequest.getDeadline(),assigneeUser);
            dataStore.addNewUserTask(task);
            if(scheduleTask) {
                dataStore.scheduleTask(task);
            } else if (bufferTask) {
                dataStore.bufferTask(task);
            }
            return new ApiResponseDTO<>(HttpStatus.OK,"User task added successfully",false);
        }catch (PermissionDenialException | IllegalArgumentException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    public ApiResponseDTO<?> scheduleTask(String taskName){
        try {
             Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
             if(taskFromDataStore == null) throw new TaskNotFoundException();
             dataStore.scheduleTask(taskFromDataStore);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task scheduled successfully",false);
        }catch (PermissionDenialException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException | TaskNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> bufferTask(String taskName) {
        try {
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) throw new TaskNotFoundException();
            dataStore.bufferTask(taskFromDataStore);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task scheduled successfully",false);
        }catch (PermissionDenialException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException | TaskNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

}
