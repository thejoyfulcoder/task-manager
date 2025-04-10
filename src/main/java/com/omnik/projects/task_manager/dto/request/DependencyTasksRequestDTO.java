package com.omnik.projects.task_manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DependencyTasksRequestDTO {
    private List<String> dependencyTasks;
}
