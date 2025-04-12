package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.request.ReportsFilterRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.enums.reports.GroupBy;
import com.omnik.projects.task_manager.enums.reports.SortBy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public interface ReportsController {

    @PostMapping("/{requester-username}")
    ResponseEntity<ApiResponseDTO<?>> deleteTask(@PathVariable("requester-username") String requesterUsername,
                                                 @RequestBody ReportsFilterRequestDTO filter, @RequestParam(name = "sortBy", required = false) SortBy sortBy, @RequestParam(name = "groupBy", required = false) GroupBy groupBy);


}
