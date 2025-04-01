package com.omnik.projects.task_manager.storage;

import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.Role;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DataStore {

    private List<User> users = new ArrayList<>();
    private Set<String> usernames= new HashSet<>();
    private EnumMap<Role,Set<Permission>> rolePermissions= new EnumMap<>(Role.class);;
    private Map<User,List<Task>> userTasks = new HashMap<>();
    private List<Task> allTasksList = new LinkedList<>();

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

    public void addNewUser(User user){
        users.add(user);
    }

    public void removeUser(User user){
        users.remove(user);
    }

    public void addUsername(String username){
        usernames.add(username);
    }

    public void deleteUsername(String username){
        usernames.remove(username);
    }

}
