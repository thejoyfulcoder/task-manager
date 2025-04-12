package com.omnik.projects.task_manager.dto.request;

import com.omnik.projects.task_manager.enums.Category;
import com.omnik.projects.task_manager.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ReportsFilterRequestDTO {
    private Integer priority;
    private TaskStatus status;
    private Category category;
    private LocalDate deadline;
    private String owner;
}
