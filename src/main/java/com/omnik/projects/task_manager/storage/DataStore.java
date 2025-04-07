package com.omnik.projects.task_manager.storage;

import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.Role;
import com.omnik.projects.task_manager.exceptions.IllegalOperationException;
import com.omnik.projects.task_manager.exceptions.UserAlreadyExistsException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataStore {

    private final Map<String,User> userMap;
    private final EnumMap<Role,Set<Permission>> rolePermissions;
    private final Map<User,List<Task>> userTasks;
    private final Map<String,Task> taskMap;
    private final TreeMap<Integer,Set<Task>>priorityGroupedTasks;
    private final PriorityQueue<Task> scheduledTasks;
    private final ArrayDeque<Task> bufferedTasks;

    public DataStore(){
        userMap = new HashMap<>();
        rolePermissions= new EnumMap<>(Role.class);
        userTasks = new HashMap<>();
        taskMap = new HashMap<>();
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

    public Map<String,Task> getAllTasks(){
        return Collections.unmodifiableMap(taskMap);
    }

    public void addNewUserTask(Task task){
        if(taskMap.putIfAbsent(task.getName(),task) == null){
            userTasks.putIfAbsent(task.getOwner(),new ArrayList<>());
            userTasks.get(task.getOwner()).add(task);
            if(task.getPriority() != null){
                priorityGroupedTasks.putIfAbsent(task.getPriority(), new HashSet<>());
                priorityGroupedTasks.get(task.getPriority()).add(task);
            }
        }else {
            throw new IllegalOperationException("A Task already exists with the passed task name");
        }
    }

    public void scheduleTask(Task incomingTask){
        boolean alreadyScheduled =scheduledTasks.stream().anyMatch((t) -> t.getName().equals(incomingTask.getName()));
        if(incomingTask.getDeadline() == null || incomingTask.getPriority() == null ){ //because the Comparator in the scheduledTasks queue requires non-null values for priority and deadline
            throw new IllegalOperationException("Deadline and priority is required for a task to be scheduled");
        } else if (alreadyScheduled) {
            throw new IllegalOperationException("This task is already scheduled. Please process it!");
        } else if (incomingTask.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalOperationException("Deadline cannot be a past Date");
        } else if (incomingTask.getOwner() == null) {
            throw new IllegalOperationException("Owner is mandatory for a task to be scheduled");
        }
        scheduledTasks.add(incomingTask);
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


}
