package com.omnik.projects.task_manager.dto.request;

import com.omnik.projects.task_manager.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TaskRequestDTO {
    private String name;
    private String description;
    private Integer priority;
    private Category category;
    private LocalDate deadline;
}
