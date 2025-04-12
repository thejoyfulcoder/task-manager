package com.omnik.projects.task_manager.storage;

import com.omnik.projects.task_manager.dto.request.ReportsFilterRequestDTO;
import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.entities.history.Operations;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.Role;
import com.omnik.projects.task_manager.enums.TaskStatus;
import com.omnik.projects.task_manager.exceptions.IllegalOperationException;
import com.omnik.projects.task_manager.exceptions.RedoStackEmptyException;
import com.omnik.projects.task_manager.exceptions.UndoStackEmptyException;
import com.omnik.projects.task_manager.exceptions.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataStore {

    private final Map<String, User> userMap;
    private final EnumMap<Role, Set<Permission>> rolePermissions;
    private final Map<String, Task> taskMap;
    private final Map<User, List<Task>> userTasks;
    private final TreeMap<Integer, HashSet<Task>> priorityGroupedTasks;
    private final PriorityQueue<Task> scheduledTasks;
    private final ArrayDeque<Task> bufferedTasks;
    private final ArrayDeque<Operations> undoStack;
    private final ArrayDeque<Operations> redoStack;
    private final Map<Task, Set<Task>> dependentTasks;

    @Value("${app.admin.user.credentials.username}")
    private String username;

    @Value("${app.admin.user.credentials.firstname}")
    private String firstName;

    @Value("${app.admin.user.credentials.lastname}")
    private String lastName;

    public DataStore() {
        log.info("Initializing DataStore");
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        userMap = new HashMap<>();
        rolePermissions = new EnumMap<>(Role.class);
        userTasks = new HashMap<>();
        taskMap = new HashMap<>();
        dependentTasks = new HashMap<>();
        priorityGroupedTasks = new TreeMap<>();
        scheduledTasks = new PriorityQueue<>(
                (t1, t2) -> {
                    int result = Integer.compare(t1.getPriority(), t2.getPriority());
                    if (result == 0) {
                        return t1.getDeadline().compareTo(t2.getDeadline());
                    }
                    return result;
                }
        );
        bufferedTasks = new ArrayDeque<>();

        HashSet<Permission> adminPermissions = new HashSet<>(Arrays.asList(Permission.values()));
        rolePermissions.put(Role.Admin, adminPermissions);
    }

    @PostConstruct
    public void init() {
        log.info("Creating initial admin user: {}", username);
        User firstAdminUser = new User(username, firstName, lastName);
        firstAdminUser.setRoles(new HashSet<>(Arrays.asList(Role.Admin)));
        userMap.put(firstAdminUser.getUsername(), firstAdminUser);
        log.debug("Admin user initialized successfully");
    }

    public Map<String, User> getAllUsers() {
        log.debug("Retrieving all users");
        return Collections.unmodifiableMap(userMap);
    }

    public Collection<Task> getAllScheduledTasks() {
        log.debug("Retrieving all scheduled tasks");
        return Collections.unmodifiableCollection(scheduledTasks);
    }

    public Collection<Task> getAllBufferedTasks() {
        log.debug("Retrieving all buffered tasks");
        return Collections.unmodifiableCollection(bufferedTasks);
    }

    public Map<String, Task> getAllTasks() {
        log.debug("Retrieving all tasks");
        return Collections.unmodifiableMap(taskMap);
    }

    public void addNewUserTask(Task task) {
        log.info("Adding new task: {}", task.getName());
        if (taskMap.putIfAbsent(task.getName(), task) == null) {
            log.debug("Task {} added to taskMap", task.getName());
            if (task.getOwner() != null) {
                userTasks.putIfAbsent(task.getOwner(), new ArrayList<>());
                userTasks.get(task.getOwner()).add(task);
                log.debug("Task {} associated with owner: {}", task.getName(), task.getOwner().getUsername());
            }
            if (task.getPriority() != null) {
                priorityGroupedTasks.putIfAbsent(task.getPriority(), new HashSet<>());
                priorityGroupedTasks.get(task.getPriority()).add(task);
                log.debug("Task {} added to priority group: {}", task.getName(), task.getPriority());
            }
        } else {
            log.error("Task already exists: {}", task.getName());
            throw new IllegalOperationException("A Task already exists with the passed task name");
        }
    }

    public void addNewOperationToUndoStack(Operations operation) {
        if (operation != null) {
            log.debug("Adding operation to undo stack");
            undoStack.push(operation);
            redoStack.clear();
            log.debug("Redo stack cleared");
        }
    }

    public void addNewOperationToRedoStack(Operations operation) {
        if (operation != null) {
            log.debug("Adding operation to redo stack");
            redoStack.push(operation);
        }
    }

    public Operations popOperationToUndo() {
        log.debug("Popping operation from undo stack");
        try {
            return undoStack.pop();
        } catch (NoSuchElementException e) {
            log.error("Undo stack is empty");
            throw new UndoStackEmptyException();
        }
    }

    public Operations popOperationToRedo() {
        log.debug("Popping operation from redo stack");
        try {
            return redoStack.pop();
        } catch (NoSuchElementException e) {
            log.error("Redo stack is empty");
            throw new RedoStackEmptyException();
        }
    }

    public void deleteTask(Task incomingTask) {
        log.info("Deleting task: {}", incomingTask.getName());
        if (taskMap.get(incomingTask.getName()) != null && taskMap.get(incomingTask.getName()).equals(incomingTask)) {
            if (incomingTask.getOwner() != null) {
                List<Task> taskMapping = userTasks.get(incomingTask.getOwner());
                if (taskMapping == null) {
                    log.error("No task mapping found for owner: {}", incomingTask.getOwner().getUsername());
                    throw new RuntimeException("Internal Server Error! the task has owner defined but there is no corresponding mapping of it in the userTasks");
                }
                taskMapping.remove(incomingTask);
                log.debug("Task {} removed from owner's task list", incomingTask.getName());
            }
            HashSet<Task> tasksForThisPriority = priorityGroupedTasks.get(incomingTask.getPriority());
            if (tasksForThisPriority != null) {
                tasksForThisPriority.remove(incomingTask);
                log.debug("Task {} removed from priority group: {}", incomingTask.getName(), incomingTask.getPriority());
            }
            scheduledTasks.remove(incomingTask);
            bufferedTasks.remove(incomingTask);
            taskMap.remove(incomingTask.getName());
            log.info("Task {} deleted successfully", incomingTask.getName());
        } else {
            log.error("Task not found or mismatch: {}", incomingTask.getName());
            throw new RuntimeException("Internal Server Error!");
        }
    }

    public void scheduleTask(Task incomingTask) {
        log.info("Scheduling task: {}", incomingTask.getName());
        if (taskMap.get(incomingTask.getName()) != null && taskMap.get(incomingTask.getName()).equals(incomingTask)) {
            if (!incomingTask.getStatus().equals(TaskStatus.Created)) {
                log.error("Invalid status for scheduling task: {}", incomingTask.getStatus());
                throw new IllegalOperationException("Invalid Request!. Only Tasks with status 'Created' can be scheduled.");
            } else if (!dependenciesCompleted(incomingTask)) {
                log.error("Dependencies not completed for task: {}", incomingTask.getName());
                throw new IllegalOperationException("A task cannot be scheduled until all its dependency tasks are completed.");
            } else if (incomingTask.getDeadline() == null || incomingTask.getPriority() == null) {
                log.error("Missing deadline or priority for task: {}", incomingTask.getName());
                throw new IllegalOperationException("Deadline and priority is required for a task to be scheduled!");
            } else if (incomingTask.getDeadline().isBefore(LocalDate.now())) {
                log.error("Past deadline for task: {}", incomingTask.getName());
                throw new IllegalOperationException("Deadline cannot be a past Date!");
            } else if (incomingTask.getOwner() == null) {
                log.error("No owner specified for task: {}", incomingTask.getName());
                throw new IllegalOperationException("Owner is mandatory for a task to be scheduled");
            }
            incomingTask.setStatus(TaskStatus.Scheduled);
            scheduledTasks.add(incomingTask);
            log.info("Task {} scheduled successfully", incomingTask.getName());
        } else {
            log.error("Task not found: {}", incomingTask.getName());
            throw new RuntimeException("Internal Server Error");
        }
    }

    private boolean dependenciesCompleted(Task task) {
        log.debug("Checking dependencies for task: {}", task.getName());
        boolean completed = task.getDependsOn().isEmpty() || task.getDependsOn().stream().allMatch(t -> t.getStatus() == TaskStatus.Completed);
        log.debug("Dependencies completed: {}", completed);
        return completed;
    }

    public void bufferTask(Task incomingTask) {
        log.info("Buffering task: {}", incomingTask.getName());
        if (taskMap.get(incomingTask.getName()) != null && taskMap.get(incomingTask.getName()).equals(incomingTask)) {
            if (incomingTask.getStatus().ordinal() >= TaskStatus.Scheduled.ordinal()) {
                log.error("Task already scheduled: {}", incomingTask.getName());
                throw new IllegalOperationException("The task is already scheduled!");
            } else if (incomingTask.getStatus().equals(TaskStatus.Buffered)) {
                log.error("Task already buffered: {}", incomingTask.getName());
                throw new IllegalOperationException("The task is already buffered. Please plan it further!");
            } else if (incomingTask.getPriority() != null || incomingTask.getDeadline() != null) {
                log.error("Buffered task has priority or deadline: {}", incomingTask.getName());
                throw new IllegalOperationException("The buffered task can not have a priority or a deadline! If it has, consider scheduling it.");
            }
            incomingTask.setStatus(TaskStatus.Buffered);
            bufferedTasks.add(incomingTask);
            log.info("Task {} buffered successfully", incomingTask.getName());
        } else {
            log.error("Task not found: {}", incomingTask.getName());
            throw new RuntimeException("Internal Server Error");
        }
    }

    public Set<Task> getTasksByPriority(Integer priority) {
        log.debug("Retrieving tasks for priority: {}", priority);
        return Collections.unmodifiableSet(priorityGroupedTasks.getOrDefault(priority, new HashSet<>()));
    }

    public Map<Role, Set<Permission>> getAllRolePermissions() {
        log.debug("Retrieving all role permissions");
        return Collections.unmodifiableMap(rolePermissions);
    }

    public void addNewUser(User user) {
        log.info("Adding new user: {}", user.getUsername());
        if (userMap.putIfAbsent(user.getUsername(), user) != null) {
            log.error("User already exists: {}", user.getUsername());
            throw new UserAlreadyExistsException();
        }
        log.info("User {} added successfully", user.getUsername());
    }

    public Task processTask(Task incomingTask) {
        log.info("Processing task: {}", incomingTask.getName());
        if (scheduledTasks.peek() != null && scheduledTasks.peek().equals(incomingTask)) {
            Task processedTask = scheduledTasks.remove();
            log.info("Task {} processed successfully", incomingTask.getName());
            return processedTask;
        } else {
            log.error("Invalid task for processing: {}", incomingTask.getName());
            throw new IllegalOperationException("Invalid request! Only high-priority tasks with immediate deadlines from the scheduled list can be processed.");
        }
    }

    public void addNewTaskDependencies(Task mainTask, Set<Task> dependencies) {
        log.info("Adding dependencies to task: {}", mainTask.getName());
        dependentTasks.putIfAbsent(mainTask, new HashSet<>());
        dependentTasks.get(mainTask).addAll(dependencies);
        log.debug("Dependencies added: {}", dependencies.stream().map(Task::getName).collect(Collectors.toList()));
    }

    public List<Task> filterBy(ReportsFilterRequestDTO filter) {
        log.info("Filtering tasks with provided filter");
        List<Task> filteredTasks = taskMap.values().stream().filter((task) -> {
            if (filter.getPriority() != null && !filter.getPriority().equals(task.getPriority())) return false;
            if (filter.getCategory() != null && !filter.getCategory().equals(task.getCategory())) return false;
            if (filter.getStatus() != null && !filter.getStatus().equals(task.getStatus())) return false;
            if (filter.getDeadline() != null && !filter.getDeadline().equals(task.getDeadline())) return false;
            return filter.getOwner() == null || filter.getOwner().equals(task.getOwner().getUsername());
        }).collect(Collectors.toList());
        log.debug("Filtered {} tasks", filteredTasks.size());
        return filteredTasks;
    }
}