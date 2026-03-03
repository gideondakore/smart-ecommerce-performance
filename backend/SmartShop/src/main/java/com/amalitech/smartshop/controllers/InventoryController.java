package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.AddInventoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateInventoryDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.InventoryResponseDTO;
import com.amalitech.smartshop.dtos.responses.LowStockDTO;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.interfaces.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for inventory management operations.
 * Handles inventory tracking and adjustments.
 */
@Tag(name = "Inventory", description = "APIs for managing product inventory")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Add inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> addInventory(@Valid @RequestBody AddInventoryDTO request) {
        InventoryResponseDTO inventory = inventoryService.addInventory(request);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory added successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all inventories")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InventoryResponseDTO>>> getAllInventories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<InventoryResponseDTO> inventories = inventoryService.getAllInventories(pageable);
        PagedResponse<InventoryResponseDTO> pagedResponse = new PagedResponse<>(
                inventories.getContent(),
                inventories.getNumber(),
                (int) inventories.getTotalElements(),
                inventories.getTotalPages(),
                inventories.isLast()
        );
        ApiResponse<PagedResponse<InventoryResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventories fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get inventory by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> getInventoryById(@PathVariable Long id) {
        InventoryResponseDTO inventory = inventoryService.getInventoryById(id);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory fetched successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get inventory by product ID")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> getInventoryByProductId(@PathVariable Long productId) {
        InventoryResponseDTO inventory = inventoryService.getInventoryByProductId(productId);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory fetched successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryDTO request) {
        InventoryResponseDTO updatedInventory = inventoryService.updateInventory(id, request);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory updated successfully", updatedInventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Adjust inventory quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> adjustInventoryQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        InventoryResponseDTO adjustedInventory = inventoryService.adjustInventoryQuantity(id, quantity);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory quantity adjusted successfully", adjustedInventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete inventory")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update stock quantity by product ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PutMapping("/product/{productId}/stock")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> updateStockByProductId(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        InventoryResponseDTO inventory = inventoryService.updateQuantityByProductId(productId, quantity);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Stock updated successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Decrement stock for a product")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping("/product/{productId}/decrement")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> decrementStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        InventoryResponseDTO inventory = inventoryService.decrementStock(productId, quantity);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Stock decremented successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Increment stock for a product")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping("/product/{productId}/increment")
    public ResponseEntity<ApiResponse<InventoryResponseDTO>> incrementStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        InventoryResponseDTO inventory = inventoryService.incrementStock(productId, quantity);
        ApiResponse<InventoryResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Stock incremented successfully", inventory);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get low stock items")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockDTO>>> getLowStockItems(
            @RequestParam(defaultValue = "10") int threshold) {
        List<LowStockDTO> lowStockItems = inventoryService.getLowStockItems(threshold);
        ApiResponse<List<LowStockDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Low stock items fetched successfully", lowStockItems);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete inventory by product ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteByProductId(@PathVariable Long productId) {
        inventoryService.deleteInventoryByProductId(productId);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Inventory deleted by product successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}
