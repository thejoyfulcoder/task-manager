package com.omnik.projects.task_manager.service;

import com.omnik.projects.task_manager.dto.request.ReportsFilterRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.enums.reports.GroupBy;
import com.omnik.projects.task_manager.enums.reports.SortBy;
import org.springframework.stereotype.Service;

@Service
public interface ReportsService {

    ApiResponseDTO<?> fetchReport(String requesterUsername,
                                  ReportsFilterRequestDTO filter, SortBy sortBy, GroupBy groupBy);

}
