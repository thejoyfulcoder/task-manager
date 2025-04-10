package com.omnik.projects.task_manager.storage;

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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataStore {

    private final Map<String,User> userMap;
    private final EnumMap<Role,Set<Permission>> rolePermissions;
    private final Map<String,Task> taskMap;
    private final Map<User,List<Task>> userTasks;
    private final TreeMap<Integer,HashSet<Task>>priorityGroupedTasks;
    private final PriorityQueue<Task> scheduledTasks;
    private final ArrayDeque<Task> bufferedTasks; //A FIFO queue for storing tasks which are just not well planned
    private final ArrayDeque<Operations> undoStack;
    private final ArrayDeque<Operations> redoStack;
    private final Map<Task,Set<Task>> dependentTasks;

    public DataStore(ArrayDeque<Operations> undoStack, ArrayDeque<Operations> redoStack, Map<Task, Set<Task>> dependentTasks){
        this.dependentTasks = dependentTasks;
        this.undoStack = new ArrayDeque<Operations>();
        this.redoStack = new ArrayDeque<Operations>();
        userMap = new HashMap<>();
        rolePermissions= new EnumMap<>(Role.class);
        userTasks = new HashMap<>();
        taskMap = new HashMap<>();
        dependentTasks = new HashMap<>();
        priorityGroupedTasks = new TreeMap<>();
        scheduledTasks = new PriorityQueue<Task>(
                (t1,t2) -> {
                   int result =Integer.compare(t1.getPriority(),t2.getPriority());
                   if(result == 0) {
                       return t1.getDeadline().compareTo(t2.getDeadline());
                   }
                   return result;
                }
        );
        bufferedTasks = new ArrayDeque<>();

        HashSet<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(Permission.Create_Task);
        adminPermissions.add(Permission.Assign_Task);
        adminPermissions.add(Permission.Delete_Task);
        adminPermissions.add(Permission.Create_User);
        adminPermissions.add(Permission.View_All);
        adminPermissions.add(Permission.Delete_User);
        rolePermissions.put(Role.Admin,adminPermissions);

        User firstAdminUser = new User("admin","Om","Nikharge");
        firstAdminUser.setRoles(new HashSet<>(Arrays.asList(Role.Admin)));
        userMap.put(firstAdminUser.getUsername(),firstAdminUser);
    }

    public Map<String,User> getAllUsers() {
        return Collections.unmodifiableMap(userMap);
    }

    public Collection<Task> getAllScheduledTasks(){
        return Collections.unmodifiableCollection(scheduledTasks);
    }



    public Collection<Task> getAllBufferedTasks(){
        return Collections.unmodifiableCollection(bufferedTasks);
    }

    public Map<String,Task> getAllTasks(){
        return Collections.unmodifiableMap(taskMap);
    }

    public void addNewUserTask(Task task){
        if(taskMap.putIfAbsent(task.getName(),task) == null){
            if(task.getOwner() != null){
                userTasks.putIfAbsent(task.getOwner(),new ArrayList<>());
                userTasks.get(task.getOwner()).add(task);
            }
            if(task.getPriority() != null){
                priorityGroupedTasks.putIfAbsent(task.getPriority(), new HashSet<>());
                priorityGroupedTasks.get(task.getPriority()).add(task);
            }
        }else {
            throw new IllegalOperationException("A Task already exists with the passed task name");
        }
    }

    public void addNewOperationToUndoStack(Operations operation){
        if(operation != null){
            undoStack.push(operation);
            redoStack.clear();
        }
    }

    public void addNewOperationToRedoStack(Operations operation){
        if(operation != null){
            redoStack.push(operation);
        }
    }

    public Operations popOperationToUndo(){
        try {
            return undoStack.pop();
        }catch (NoSuchElementException e){
            throw new UndoStackEmptyException();
        }
    }

    public Operations popOperationToRedo(){
        try {
            return redoStack.pop();
        }catch (NoSuchElementException e){
            throw new RedoStackEmptyException();
        }
    }

    public void deleteTask(Task incomingTask){
        if(taskMap.get(incomingTask.getName()) != null && taskMap.get(incomingTask.getName()).equals(incomingTask)){
            if(incomingTask.getOwner() != null){
                List<Task> taskMapping =  userTasks.remove(incomingTask.getOwner());
                if(taskMapping == null){
                    throw new RuntimeException("Internal Server Error! the task has owner defined but there is no corresponding mapping of it in the userTasks");
                }
            }
            HashSet<Task> tasksForThisPriority = priorityGroupedTasks.get(incomingTask.getPriority());
            if(tasksForThisPriority != null){
                tasksForThisPriority.remove(incomingTask);
            }
            scheduledTasks.remove(incomingTask);
            bufferedTasks.remove(incomingTask);
            taskMap.remove(incomingTask.getName());
        }else{
            throw new RuntimeException("Internal Server Error!");
        }
    }

    public void scheduleTask(Task incomingTask){
        if(taskMap.get(incomingTask.getName()) != null && taskMap.get(incomingTask.getName()).equals(incomingTask)) {
            if (!incomingTask.getStatus().equals(TaskStatus.Created)) {
                throw new IllegalOperationException("Invalid Request!. Only Tasks with status 'Created' can be scheduled.");
            } else if (!dependenciesCompleted(incomingTask)) {
                throw new IllegalOperationException("A task cannot be scheduled until all its dependency tasks are completed.");
            } else if(incomingTask.getDeadline() == null || incomingTask.getPriority() == null ){ //because the Comparator in the scheduledTasks queue requires non-null values for priority and deadline
                throw new IllegalOperationException("Deadline and priority is required for a task to be scheduled!");
            }
            else if (incomingTask.getDeadline().isBefore(LocalDate.now())) {
                throw new IllegalOperationException("Deadline cannot be a past Date!");
            }
            else if (incomingTask.getOwner() == null) {
                throw new IllegalOperationException("Owner is mandatory for a task to be scheduled");
            }
            incomingTask.setStatus(TaskStatus.Scheduled);
            scheduledTasks.add(incomingTask);
        }else {
            throw new RuntimeException("Internal Server Error");
        }
    }

    private boolean dependenciesCompleted(Task task) {
        return task.getDependsOn().isEmpty() || task.getDependsOn().stream().allMatch(t -> t.getStatus() == TaskStatus.Completed);
    }

    public void bufferTask(Task incomingTask){
        if(taskMap.get(incomingTask.getName()) != null && taskMap.get(incomingTask.getName()).equals(incomingTask)){
            if(incomingTask.getStatus().ordinal() >= TaskStatus.Scheduled.ordinal()){
                throw new IllegalOperationException("The task is already scheduled!");
            } else if (incomingTask.getStatus().equals(TaskStatus.Buffered)) {
                throw new IllegalOperationException("The task is already buffered. Please plan it further!");
            } else if(incomingTask.getPriority() != null || incomingTask.getDeadline() != null){
                throw new IllegalOperationException("The buffered task can not have a priority or a deadline! If it has, consider scheduling it.");
            }
            incomingTask.setStatus(TaskStatus.Buffered);
            bufferedTasks.add(incomingTask);
        }else {
            throw new RuntimeException("Internal Server Error");
        }

    }

    public Set<Task> getTasksByPriority(Integer priority){
        return Collections.unmodifiableSet(priorityGroupedTasks.get(priority));
    }

    public Map<Role,Set<Permission>> getAllRolePermissions(){
        return Collections.unmodifiableMap(rolePermissions);
    }

    public void addNewUser(User user){
        if(userMap.putIfAbsent(user.getUsername(),user) != null) throw new UserAlreadyExistsException();
    }


    public Task processTask(Task incomingTask) {
           if(scheduledTasks.peek() != null && scheduledTasks.peek().equals(incomingTask)){
               return scheduledTasks.remove();
           }else {
               throw new IllegalOperationException("Invalid request! Only high-priority tasks with immediate deadlines from the scheduled list can be processed.");
           }
    }

    public void addNewTaskDependencies(Task mainTask,Set<Task> dependencies){
        dependentTasks.putIfAbsent(mainTask,new HashSet<>());
        dependentTasks.get(mainTask).addAll(dependencies);
    }
}
