package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.config.RequiresRole;
import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.UpdateOrderDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.OrderService;
import com.amalitech.smartshop.utils.sorting.SortingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for order management operations.
 * Handles order creation, retrieval, and status updates.
 */
@Tag(name = "Order Management", description = "APIs for managing orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SortingService sortingService;

    @Operation(summary = "Create a new order")
    @RequiresRole({UserRole.CUSTOMER, UserRole.ADMIN})
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @Valid @RequestBody AddOrderDTO request,
            HttpServletRequest httpRequest) {
        Long authUserId = (Long) httpRequest.getAttribute("authUserId");
        request.setUserId(authUserId);
        OrderResponseDTO order = orderService.createOrder(request);
        ApiResponse<OrderResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order created successfully", order);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all orders")
    @RequiresRole({UserRole.ADMIN, UserRole.VENDOR})
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
    @RequiresRole({UserRole.CUSTOMER, UserRole.ADMIN})
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponseDTO>>> getOrdersByUserId(
            HttpServletRequest httpRequest,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long authUserId = (Long) httpRequest.getAttribute("authUserId");
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
    @RequiresRole(UserRole.ADMIN)
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
    @RequiresRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Order deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}
