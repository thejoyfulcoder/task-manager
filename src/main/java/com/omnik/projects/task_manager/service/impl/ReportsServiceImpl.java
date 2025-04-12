package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.request.ReportsFilterRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.reports.GroupBy;
import com.omnik.projects.task_manager.enums.reports.SortBy;
import com.omnik.projects.task_manager.exceptions.PermissionDenialException;
import com.omnik.projects.task_manager.exceptions.UserNotFoundException;
import com.omnik.projects.task_manager.service.ReportsService;
import com.omnik.projects.task_manager.service.UserService;
import com.omnik.projects.task_manager.storage.DataStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReportsServiceImpl implements ReportsService {

    private final UserService userService;
    private final DataStore dataStore;

    public ReportsServiceImpl(UserService userService, DataStore dataStore) {
        this.userService = userService;
        this.dataStore = dataStore;
    }

    @Override
    public ApiResponseDTO<?> fetchReport(String requesterUsername, ReportsFilterRequestDTO filter, SortBy sortBy, GroupBy groupBy) {
        log.info("Fetching report for user: {} with sortBy: {} and groupBy: {}", requesterUsername, sortBy, groupBy);
        try {
            log.debug("Validating user: {} for view all permission", requesterUsername);
            userService.validateUser(requesterUsername, Permission.View_All);

            log.debug("Filtering tasks based on provided filter");
            List<Task> filteredTasks = dataStore.filterBy(filter);

            Map<?, List<Task>> map = new HashMap<>();
            if (groupBy != null) {
                log.debug("Grouping tasks by: {}", groupBy);
                switch (groupBy) {
                    case Owner: {
                        map = filteredTasks.stream().collect(Collectors.groupingBy(Task::getOwner));
                        break;
                    }
                    case Status: {
                        map = filteredTasks.stream().collect(Collectors.groupingBy(Task::getStatus));
                        break;
                    }
                    case Priority: {
                        map = filteredTasks.stream().collect(Collectors.groupingBy(Task::getPriority));
                        break;
                    }
                    case Category: {
                        map = filteredTasks.stream().collect(Collectors.groupingBy(Task::getCategory));
                        break;
                    }
                    case Deadline: {
                        map = filteredTasks.stream().collect(Collectors.groupingBy(Task::getDeadline));
                        break;
                    }
                }
            } else {
                log.debug("No grouping applied, adding all tasks to map");
                map.put(null, filteredTasks);
            }

            if (sortBy != null) {
                log.debug("Sorting tasks by: {}", sortBy);
                Comparator<Task> customComparator = sortBy.equals(SortBy.Priority)
                        ? Comparator.comparing(Task::getPriority, Comparator.nullsLast(Comparator.naturalOrder()))
                        : Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
                map.forEach((key, taskList) -> {
                    log.trace("Sorting task list for key: {}", key);
                    taskList.sort(customComparator);
                });
            }

            log.info("Report fetched successfully for user: {}", requesterUsername);
            return new ApiResponseDTO<>(map, null, HttpStatus.OK, "Reports fetched successfully", false);
        } catch (PermissionDenialException e) {
            log.error("Permission error while fetching report for user: {}. Error: {}", requesterUsername, e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(), true);
        } catch (UserNotFoundException nfe) {
            log.error("User not found while fetching report: {}. Error: {}", requesterUsername, nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(), true);
        } catch (Exception e) {
            log.error("Unexpected error while fetching report for user: {}. Error: {}", requesterUsername, e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), true);
        }
    }
}