package com.omnik.projects.task_manager.controller.impl;

import com.omnik.projects.task_manager.controller.ReportsController;
import com.omnik.projects.task_manager.dto.request.ReportsFilterRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.enums.reports.GroupBy;
import com.omnik.projects.task_manager.enums.reports.SortBy;
import com.omnik.projects.task_manager.service.ReportsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportsControllerImpl implements ReportsController {

    private final ReportsService reportsService;

    public ReportsControllerImpl(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> deleteTask(String requesterUsername, ReportsFilterRequestDTO filter, SortBy sortBy, GroupBy groupBy) {
        return ResponseEntity.ok(reportsService.fetchReport(requesterUsername,filter,sortBy,groupBy));
    }
}
