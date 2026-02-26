package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.config.RequiresRole;
import com.amalitech.smartshop.dtos.requests.AddCategoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCategoryDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.CategoryResponseDTO;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for category management operations.
 * Handles CRUD operations for product categories.
 */
@Tag(name = "Category Management", description = "APIs for managing product categories")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Add a new category")
    @RequiresRole(UserRole.ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> addCategory(@Valid @RequestBody AddCategoryDTO request) {
        // I create a new category and return the response
        CategoryResponseDTO category = categoryService.addCategory(request);
        ApiResponse<CategoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.CREATED.value(), "Category added successfully", category);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @Operation(summary = "Get all categories")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponseDTO>>> getAllCategories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // I retrieve all categories with pagination
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<CategoryResponseDTO> categories = categoryService.getAllCategories(pageable);
        PagedResponse<CategoryResponseDTO> pagedResponse = new PagedResponse<>(
                categories.getContent(),
                categories.getNumber(),
                (int) categories.getTotalElements(),
                categories.getTotalPages(),
                categories.isLast()
        );
        ApiResponse<PagedResponse<CategoryResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Categories fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        ApiResponse<CategoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Category fetched successfully", category);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update a category")
    @RequiresRole(UserRole.ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryDTO request) {
        // I update the category with the given ID
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, request);
        ApiResponse<CategoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Category updated successfully", updatedCategory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a category")
    @RequiresRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Category deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}
