package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.aspects.PerformanceMonitoringAspect;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Performance Monitoring", description = "APIs for monitoring database performance")
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceMonitoringAspect performanceAspect;

    public PerformanceController(PerformanceMonitoringAspect performanceAspect) {
        this.performanceAspect = performanceAspect;
    }

    @Operation(summary = "Get database fetch times", description = "Retrieves all recorded database query execution times. Requires ADMIN role.",
            security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/db-metrics")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Object>>>> getDbMetrics() {
        Map<String, Map<String, Object>> metrics = performanceAspect.getDbFetchTimes();
        ApiResponse<Map<String, Map<String, Object>>> response = new ApiResponse<>(HttpStatus.OK.value(), "Performance metrics retrieved successfully", metrics);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Clear performance metrics", description = "Clears all recorded performance metrics. Requires ADMIN role.",
            security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/clear-metrics")
    public ResponseEntity<ApiResponse<Void>> clearMetrics() {
        performanceAspect.clearMetrics();
        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), "Performance metrics cleared successfully", null);
        return ResponseEntity.ok(response);
    }
}