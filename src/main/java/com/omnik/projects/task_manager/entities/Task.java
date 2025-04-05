package com.omnik.projects.task_manager.entities;

import com.omnik.projects.task_manager.enums.Category;
import com.omnik.projects.task_manager.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class Task {
    private String name;
    private String description;
    private Integer priority;
    private TaskStatus status;
    private Category category;
    private Date deadline;
}
