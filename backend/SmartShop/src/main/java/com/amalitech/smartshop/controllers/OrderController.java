package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.UpdateOrderDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.BestSellerDTO;
import com.amalitech.smartshop.dtos.responses.OrderItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.dtos.responses.RevenueReportDTO;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.interfaces.OrderService;
import com.amalitech.smartshop.utils.sorting.SortingService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for order management operations.
 * Handles order creation, retrieval, and status updates.
 */
@Tag(name = "Orders", description = "APIs for managing orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final SortingService sortingService;

    @Operation(summary = "Create a new order")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @Valid @RequestBody AddOrderDTO request,
            @AuthenticationPrincipal User currentUser) {
        request.setUserId(currentUser.getId());
        OrderResponseDTO order = orderService.createOrder(request);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.CREATED.value(), "Order created successfully", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @Operation(summary = "Get all orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR', 'STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponseDTO>>> getAllOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "ascending", defaultValue = "false") boolean ascending,
            @RequestParam(value = "algorithm", defaultValue = "MERGESORT") String algorithm
    ) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);
        List<OrderResponseDTO> orderList = new ArrayList<>(orders.getContent());

        // Apply custom sorting if sortBy is specified
        if (sortBy != null) {
            try {
                SortingService.OrderSortField field = SortingService.OrderSortField.valueOf(sortBy.toUpperCase());
                SortingService.SortAlgorithm algo = SortingService.SortAlgorithm.valueOf(algorithm.toUpperCase());
                sortingService.sortOrders(orderList, field, ascending, algo);
            } catch (IllegalArgumentException e) {
                // Invalid sortBy or algorithm, ignore and return unsorted
            }
        }

        PagedResponse<OrderResponseDTO> pagedResponse = new PagedResponse<>(
                orderList,
                orders.getNumber(),
                (int) orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
        ApiResponse<PagedResponse<OrderResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Orders fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get orders by user")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponseDTO>>> getOrdersByUserId(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long authUserId = currentUser.getId();
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<OrderResponseDTO> orders = orderService.getOrdersByUserId(authUserId, pageable);
        PagedResponse<OrderResponseDTO> pagedResponse = new PagedResponse<>(
                orders.getContent(),
                orders.getNumber(),
                (int) orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
        ApiResponse<PagedResponse<OrderResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User orders fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get order by ID")
    // @RequiresRole(UserRole.CUSTOMER)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(@PathVariable Long id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order fetched successfully", order);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update order status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderDTO request) {
        // I update the order status for the given order ID
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, request);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order status updated successfully", updatedOrder);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete an order")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get order items")
    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<OrderItemResponseDTO>>> getOrderItems(@PathVariable Long id) {
        List<OrderItemResponseDTO> items = orderService.getOrderItems(id);
        ApiResponse<List<OrderItemResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order items fetched successfully", items);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get high value orders")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/high-value")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponseDTO>>> getHighValueOrders(
            @RequestParam Double minAmount,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<OrderResponseDTO> orders = orderService.getHighValueOrders(minAmount, pageable);
        PagedResponse<OrderResponseDTO> pagedResponse = new PagedResponse<>(
                orders.getContent(),
                orders.getNumber(),
                (int) orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
        ApiResponse<PagedResponse<OrderResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "High value orders fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get revenue report")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<RevenueReportDTO>>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<RevenueReportDTO> report = orderService.getRevenueReport(startDate, endDate);
        ApiResponse<List<RevenueReportDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Revenue report fetched successfully", report);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get best selling products")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/best-sellers")
    public ResponseEntity<ApiResponse<List<BestSellerDTO>>> getBestSellers(
            @RequestParam(defaultValue = "10") int limit) {
        List<BestSellerDTO> bestSellers = orderService.getBestSellingProducts(limit);
        ApiResponse<List<BestSellerDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Best sellers fetched successfully", bestSellers);
        return ResponseEntity.ok(apiResponse);
    }
}
