package com.omnik.projects.task_manager.entities;

import com.omnik.projects.task_manager.enums.Category;
import com.omnik.projects.task_manager.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Task {
    private String name;
    private String description;
    private Integer priority;
    private TaskStatus status = TaskStatus.Created;
    private Category category;
    private LocalDate deadline;
    private Set<Task> dependsOn = new HashSet<>();
    private User owner;

    public Task(String name, String description, Integer priority, Category category, LocalDate deadline,User owner) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.deadline = deadline;
        this.owner = owner;
    }

    public Task(String name, String description,Category category, User owner) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.owner = owner;
    }


}
