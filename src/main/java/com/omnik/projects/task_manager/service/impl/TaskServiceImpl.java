package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.request.DependencyTasksRequestDTO;
import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.entities.history.TaskCreationAndDeletionOperation;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.TaskStatus;
import com.omnik.projects.task_manager.exceptions.IllegalOperationException;
import com.omnik.projects.task_manager.exceptions.PermissionDenialException;
import com.omnik.projects.task_manager.exceptions.TaskNotFoundException;
import com.omnik.projects.task_manager.exceptions.UserNotFoundException;
import com.omnik.projects.task_manager.service.TaskService;
import com.omnik.projects.task_manager.service.UserService;
import com.omnik.projects.task_manager.storage.DataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

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
                    taskCreationRequest.getCategory(),taskCreationRequest.getDeadline(),userFromDataStore);
            dataStore.addNewUserTask(task);
            if(scheduleTask) {
                dataStore.scheduleTask(task);
            } else if (bufferTask) {
                dataStore.bufferTask(task);
            }
            //Adding to the undo stack
            dataStore.addNewOperationToUndoStack(new TaskCreationAndDeletionOperation(dataStore,task,scheduleTask,bufferTask,true));
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
    public ApiResponseDTO<?> deleteTask(String requesterUsername, String taskName) {
        try {
            userService.validateUser(requesterUsername, Permission.Delete_Task);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) throw new TaskNotFoundException();
            boolean isTaskScheduled = dataStore.getAllScheduledTasks().contains(taskFromDataStore);
            boolean isTaskBuffered = dataStore.getAllBufferedTasks().contains(taskFromDataStore);
            dataStore.deleteTask(taskFromDataStore);
            dataStore.addNewOperationToUndoStack(new TaskCreationAndDeletionOperation(dataStore,taskFromDataStore,isTaskScheduled,isTaskBuffered,false));
            return new ApiResponseDTO<>(HttpStatus.OK,"Task deleted successfully",false);
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
                  taskCreationRequest.getCategory(),taskCreationRequest.getDeadline(),assigneeUser);
            dataStore.addNewUserTask(task);
            if(scheduleTask) {
                dataStore.scheduleTask(task);
            } else if (bufferTask) {
                dataStore.bufferTask(task);
            }
            dataStore.addNewOperationToUndoStack(new TaskCreationAndDeletionOperation(dataStore,task,scheduleTask,bufferTask,true));
            return new ApiResponseDTO<>(HttpStatus.OK,"Task assigned successfully",false);
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
            return new ApiResponseDTO<>(HttpStatus.OK,"Task buffered successfully",false);
        }catch (PermissionDenialException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException | TaskNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> processTask(String requesterUsername, String taskName, TaskStatus status) {
        try {
            userService.validateUser(requesterUsername,Permission.Update_Task);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) throw new TaskNotFoundException();
            Task taskToBeProcessed =dataStore.processTask(taskFromDataStore);
            if(!(status == TaskStatus.InProgress || status == TaskStatus.Completed)) throw new IllegalOperationException("Task status can only be updated as 'InProgress' or 'Completed'");
            taskToBeProcessed.setStatus(status);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task processed successfully",false);
        }catch (PermissionDenialException | IllegalOperationException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException | TaskNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> markCompleted(String requesterUsername, String taskName) {
        try {
            userService.validateUser(requesterUsername,Permission.Update_Task);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) throw new TaskNotFoundException();
            if(taskFromDataStore.getStatus() != TaskStatus.InProgress) throw new IllegalOperationException("Tasks that are only in progress can be marked as completed!");
            taskFromDataStore.setStatus(TaskStatus.Completed);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task marked as completed successfully",false);
        }catch (PermissionDenialException | IllegalOperationException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException | TaskNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> addDependencyTasks(String requesterUsername, String taskName, DependencyTasksRequestDTO dependencyTasks) {
        try {
            userService.validateUser(requesterUsername,Permission.Update_Task);
            Task mainTask = dataStore.getAllTasks().get(taskName);
            if(mainTask == null) throw new TaskNotFoundException();
            if(mainTask.getStatus().ordinal() >= TaskStatus.Scheduled.ordinal()) throw new IllegalOperationException("Dependencies can only be added to a task which is unscheduled!");
            Set<Task> tasksFromDataStore = new HashSet<>();
            dependencyTasks.getDependencyTasks().forEach((tName) -> {
                Task task =  dataStore.getAllTasks().get(tName);
                 if(task == null){
                     throw new TaskNotFoundException("No task found for the passed taskName: "+tName);
                 } else if (task.getStatus().equals(TaskStatus.Buffered)) {
                     throw new IllegalOperationException("The task "+tName+" is a buffered task and cannot be added as a dependency.");
                 }
                if(!tasksFromDataStore.add(task)){
                    throw new IllegalOperationException("Dependency tasks cannot be duplicate!");
                }
            });
            mainTask.getDependsOn().addAll(tasksFromDataStore);
            dataStore.addNewTaskDependencies(mainTask,tasksFromDataStore);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task dependencies added successfully.",false);
        }catch (PermissionDenialException | IllegalOperationException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException | TaskNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

}
