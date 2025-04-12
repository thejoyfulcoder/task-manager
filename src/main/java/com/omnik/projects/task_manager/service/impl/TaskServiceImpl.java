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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class TaskServiceImpl implements TaskService {

    private final UserService userService;
    private final DataStore dataStore;

    public TaskServiceImpl(UserService userService, DataStore dataStore) {
        this.userService = userService;
        this.dataStore = dataStore;
    }

    @Override
    public ApiResponseDTO<?> createTask(String requesterUsername, boolean scheduleTask, boolean bufferTask, TaskRequestDTO taskCreationRequest) {
        log.info("Creating task for user: {}, scheduleTask: {}, bufferTask: {}", requesterUsername, scheduleTask, bufferTask);
        try {
            log.debug("Validating user: {} for task creation permission", requesterUsername);
            User userFromDataStore = userService.validateUser(requesterUsername, Permission.Create_Task);

            log.debug("Creating new task with name: {}, priority: {}, category: {}",
                    taskCreationRequest.getName(), taskCreationRequest.getPriority(), taskCreationRequest.getCategory());
            Task task = new Task(taskCreationRequest.getName(),
                    taskCreationRequest.getDescription(),
                    taskCreationRequest.getPriority(),
                    taskCreationRequest.getCategory(),
                    taskCreationRequest.getDeadline(),
                    userFromDataStore);

            log.debug("Adding task to data store");
            dataStore.addNewUserTask(task);

            if(scheduleTask) {
                log.debug("Scheduling task: {}", task.getName());
                dataStore.scheduleTask(task);
            } else if (bufferTask) {
                log.debug("Buffering task: {}", task.getName());
                dataStore.bufferTask(task);
            }

            log.debug("Adding task creation operation to undo stack");
            dataStore.addNewOperationToUndoStack(new TaskCreationAndDeletionOperation(dataStore,task,scheduleTask,bufferTask,true));

            log.info("Task created successfully for user: {}", requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK,"User task added successfully",false);
        } catch (PermissionDenialException | IllegalOperationException e) {
            log.error("Permission or operation error while creating task for user: {}. Error: {}", requesterUsername, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException nfe) {
            log.error("User not found while creating task: {}. Error: {}", requesterUsername, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while creating task for user: {}. Error: {}", requesterUsername, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> deleteTask(String requesterUsername, String taskName) {
        log.info("Deleting task: {} for user: {}", taskName, requesterUsername);
        try {
            log.debug("Validating user: {} for task deletion permission", requesterUsername);
            userService.validateUser(requesterUsername, Permission.Delete_Task);

            log.debug("Retrieving task: {} from data store", taskName);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) {
                log.warn("Task not found: {}", taskName);
                throw new TaskNotFoundException();
            }

            log.debug("Checking task status for scheduled/buffered");
            boolean isTaskScheduled = dataStore.getAllScheduledTasks().contains(taskFromDataStore);
            boolean isTaskBuffered = dataStore.getAllBufferedTasks().contains(taskFromDataStore);

            log.debug("Deleting task: {}", taskName);
            dataStore.deleteTask(taskFromDataStore);

            log.debug("Adding deletion operation to undo stack");
            dataStore.addNewOperationToUndoStack(new TaskCreationAndDeletionOperation(dataStore,taskFromDataStore,isTaskScheduled,isTaskBuffered,false));

            log.info("Task {} deleted successfully for user: {}", taskName, requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task deleted successfully",false);
        } catch (PermissionDenialException | IllegalOperationException e) {
            log.error("Permission or operation error while deleting task: {}. Error: {}", taskName, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException nfe) {
            log.error("User not found while deleting task: {}. Error: {}", taskName, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while deleting task: {}. Error: {}", taskName, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> assignTask(String requesterUsername, String assigneeUsername, boolean scheduleTask, boolean bufferTask, TaskRequestDTO taskCreationRequest) {
        log.info("Assigning task to user: {} by requester: {}, scheduleTask: {}, bufferTask: {}", assigneeUsername, requesterUsername, scheduleTask, bufferTask);
        try {
            log.debug("Validating requester: {} for task creation permission", requesterUsername);
            userService.validateUser(requesterUsername,Permission.Create_Task);

            log.debug("Validating assignee user: {}", assigneeUsername);
            User assigneeUser= userService.validateUser(assigneeUsername,null);

            log.debug("Creating new task with name: {}, priority: {}, category: {}",
                    taskCreationRequest.getName(), taskCreationRequest.getPriority(), taskCreationRequest.getCategory());
            Task task = new Task(taskCreationRequest.getName(),
                    taskCreationRequest.getDescription(),
                    taskCreationRequest.getPriority(),
                    taskCreationRequest.getCategory(),
                    taskCreationRequest.getDeadline(),
                    assigneeUser);

            log.debug("Adding task to data store");
            dataStore.addNewUserTask(task);

            if(scheduleTask) {
                log.debug("Scheduling task: {}", task.getName());
                dataStore.scheduleTask(task);
            } else if (bufferTask) {
                log.debug("Buffering task: {}", task.getName());
                dataStore.bufferTask(task);
            }

            log.debug("Adding task assignment operation to undo stack");
            dataStore.addNewOperationToUndoStack(new TaskCreationAndDeletionOperation(dataStore,task,scheduleTask,bufferTask,true));

            log.info("Task assigned successfully to user: {} by requester: {}", assigneeUsername, requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task assigned successfully",false);
        } catch (PermissionDenialException | IllegalArgumentException e) {
            log.error("Permission or argument error while assigning task to user: {}. Error: {}", assigneeUsername, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException nfe) {
            log.error("User not found while assigning task to user: {}. Error: {}", assigneeUsername, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while assigning task to user: {}. Error: {}", assigneeUsername, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    public ApiResponseDTO<?> scheduleTask(String taskName) {
        log.info("Scheduling task: {}", taskName);
        try {
            log.debug("Retrieving task: {} from data store", taskName);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) {
                log.warn("Task not found: {}", taskName);
                throw new TaskNotFoundException();
            }

            log.debug("Scheduling task: {}", taskName);
            dataStore.scheduleTask(taskFromDataStore);

            log.info("Task {} scheduled successfully", taskName);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task scheduled successfully",false);
        } catch (PermissionDenialException e) {
            log.error("Permission error while scheduling task: {}. Error: {}", taskName, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException | TaskNotFoundException nfe) {
            log.error("User or task not found while scheduling task: {}. Error: {}", taskName, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while scheduling task: {}. Error: {}", taskName, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> bufferTask(String taskName) {
        log.info("Buffering task: {}", taskName);
        try {
            log.debug("Retrieving task: {} from data store", taskName);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) {
                log.warn("Task not found: {}", taskName);
                throw new TaskNotFoundException();
            }

            log.debug("Buffering task: {}", taskName);
            dataStore.bufferTask(taskFromDataStore);

            log.info("Task {} buffered successfully", taskName);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task buffered successfully",false);
        } catch (PermissionDenialException e) {
            log.error("Permission error while buffering task: {}. Error: {}", taskName, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException | TaskNotFoundException nfe) {
            log.error("User or task not found while buffering task: {}. Error: {}", taskName, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while buffering task: {}. Error: {}", taskName, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> processTask(String requesterUsername, String taskName, TaskStatus status) {
        log.info("Processing task: {} for user: {} with status: {}", taskName, requesterUsername, status);
        try {
            log.debug("Validating user: {} for task update permission", requesterUsername);
            userService.validateUser(requesterUsername,Permission.Update_Task);

            log.debug("Retrieving task: {} from data store", taskName);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) {
                log.warn("Task not found: {}", taskName);
                throw new TaskNotFoundException();
            }

            log.debug("Processing task: {}", taskName);
            Task taskToBeProcessed =dataStore.processTask(taskFromDataStore);
            if(!(status == TaskStatus.InProgress || status == TaskStatus.Completed)) {
                log.warn("Invalid status update attempted for task: {}. Status: {}", taskName, status);
                throw new IllegalOperationException("Task status can only be updated as 'InProgress' or 'Completed'");
            }

            log.debug("Setting task status to: {}", status);
            taskToBeProcessed.setStatus(status);

            log.info("Task {} processed successfully for user: {}", taskName, requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task processed successfully",false);
        } catch (PermissionDenialException | IllegalOperationException e) {
            log.error("Permission or operation error while processing task: {}. Error: {}", taskName, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException | TaskNotFoundException nfe) {
            log.error("User or task not found while processing task: {}. Error: {}", taskName, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while processing task: {}. Error: {}", taskName, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> markCompleted(String requesterUsername, String taskName) {
        log.info("Marking task as completed: {} for user: {}", taskName, requesterUsername);
        try {
            log.debug("Validating user: {} for task update permission", requesterUsername);
            userService.validateUser(requesterUsername,Permission.Update_Task);

            log.debug("Retrieving task: {} from data store", taskName);
            Task taskFromDataStore = dataStore.getAllTasks().get(taskName);
            if(taskFromDataStore == null) {
                log.warn("Task not found: {}", taskName);
                throw new TaskNotFoundException();
            }

            if(taskFromDataStore.getStatus() != TaskStatus.InProgress) {
                log.warn("Invalid status for completion: {} for task: {}", taskFromDataStore.getStatus(), taskName);
                throw new IllegalOperationException("Tasks that are only in progress can be marked as completed!");
            }

            log.debug("Setting task status to Completed");
            taskFromDataStore.setStatus(TaskStatus.Completed);

            log.info("Task {} marked as completed successfully for user: {}", taskName, requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task marked as completed successfully",false);
        } catch (PermissionDenialException | IllegalOperationException e) {
            log.error("Permission or operation error while marking task completed: {}. Error: {}", taskName, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException | TaskNotFoundException nfe) {
            log.error("User or task not found while marking task completed: {}. Error: {}", taskName, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while marking task completed: {}. Error: {}", taskName, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> addDependencyTasks(String requesterUsername, String taskName, DependencyTasksRequestDTO dependencyTasks) {
        log.info("Adding dependencies to task: {} for user: {}", taskName, requesterUsername);
        try {
            log.debug("Validating user: {} for task update permission", requesterUsername);
            userService.validateUser(requesterUsername,Permission.Update_Task);

            log.debug("Retrieving main task: {} from data store", taskName);
            Task mainTask = dataStore.getAllTasks().get(taskName);
            if(mainTask == null) {
                log.warn("Main task not found: {}", taskName);
                throw new TaskNotFoundException();
            }

            if(mainTask.getStatus().ordinal() >= TaskStatus.Scheduled.ordinal()) {
                log.warn("Cannot add dependencies to scheduled task: {}", taskName);
                throw new IllegalOperationException("Dependencies can only be added to a task which is unscheduled!");
            }

            log.debug("Processing dependency tasks");
            Set<Task> tasksFromDataStore = new HashSet<>();
            dependencyTasks.getDependencyTasks().forEach((tName) -> {
                log.debug("Validating dependency task: {}", tName);
                Task task =  dataStore.getAllTasks().get(tName);
                if(task == null){
                    log.warn("Dependency task not found: {}", tName);
                    throw new TaskNotFoundException("No task found for the passed taskName: "+tName);
                } else if (task.getStatus().equals(TaskStatus.Buffered)) {
                    log.warn("Cannot add buffered task as dependency: {}", tName);
                    throw new IllegalOperationException("The task "+tName+" is a buffered task and cannot be added as a dependency.");
                }
                if(!tasksFromDataStore.add(task)){
                    log.warn("Duplicate dependency task detected: {}", tName);
                    throw new IllegalOperationException("Dependency tasks cannot be duplicate!");
                }
            });

            log.debug("Adding dependencies to main task");
            mainTask.getDependsOn().addAll(tasksFromDataStore);
            dataStore.addNewTaskDependencies(mainTask,tasksFromDataStore);

            log.info("Dependencies added successfully to task: {} for user: {}", taskName, requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK,"Task dependencies added successfully.",false);
        } catch (PermissionDenialException | IllegalOperationException e) {
            log.error("Permission or operation error while adding dependencies to task: {}. Error: {}", taskName, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        } catch (UserNotFoundException | TaskNotFoundException nfe) {
            log.error("User or task not found while adding dependencies to task: {}. Error: {}", taskName, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        } catch (Exception e) {
            log.error("Unexpected error while adding dependencies to task: {}. Error: {}", taskName, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }
}