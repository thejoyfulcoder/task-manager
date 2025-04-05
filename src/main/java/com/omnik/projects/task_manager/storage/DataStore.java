package com.omnik.projects.task_manager.storage;

import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.Role;
import com.omnik.projects.task_manager.exceptions.UserAlreadyExistsException;
import com.omnik.projects.task_manager.exceptions.UserNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataStore {

    private List<User> users = new ArrayList<>();
    private Set<String> usernames= new HashSet<>();
    private EnumMap<Role,Set<Permission>> rolePermissions= new EnumMap<>(Role.class);;
    private Map<User,List<Task>> userTasks = new HashMap<>();
    private List<Task> allTasksList = new LinkedList<>();
    private TreeMap<Integer,Set<Task>> priorityGroupedTasks = new TreeMap<>();

    public DataStore(){
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
        usernames.add("admin");
        users.add(firstAdminUser);
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public Set<String> getUsernames() {
        return Collections.unmodifiableSet(usernames);
    }

    public List<Task> getAllTasksList(){
        return Collections.unmodifiableList(allTasksList);
    }

    public void addNewUserTask(User user,Task task){
        userTasks.putIfAbsent(user,new ArrayList<>());
        userTasks.get(user).add(task);
        allTasksList.add(task);
        if(task.getPriority() != null){
            priorityGroupedTasks.putIfAbsent(task.getPriority(), new HashSet<>());
            priorityGroupedTasks.get(task.getPriority()).add(task);
        }
    }

    public Set<Task> getTasksByPriority(Integer priority){
        return Collections.unmodifiableSet(priorityGroupedTasks.get(priority));
    }

    public Map<Role,Set<Permission>> getAllRolePermissions(){
        return Collections.unmodifiableMap(rolePermissions);
    }

    public void addNewUser(User user){
        if(usernames.add(user.getUsername())){
            users.add(user);
        }else{
            throw new UserAlreadyExistsException();
        }
    }

    public void removeUser(User user){
        if(usernames.remove(user.getUsername())){
            users.remove(user);
        }else{
            throw new UserNotFoundException();
        }
    }

}
