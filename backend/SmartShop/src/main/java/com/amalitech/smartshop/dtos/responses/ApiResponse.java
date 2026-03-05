package com.amalitech.smartshop.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic API response wrapper for successful operations")
public record ApiResponse<T>(
        @Schema(description = "HTTP status code", example = "200")
        int status,

        @Schema(description = "Response message", example = "Operation successful")
        String message,

        @Schema(description = "Response data payload")
        T data
) {
        
}
