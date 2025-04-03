package com.omnik.projects.task_manager.dto.request;

import com.omnik.projects.task_manager.enums.Category;
import com.omnik.projects.task_manager.enums.Priority;
import com.omnik.projects.task_manager.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class TaskRequestDTO {
    private String name;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private Category category;
    private Date deadline;
}
