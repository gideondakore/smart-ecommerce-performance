package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.UpdateOrderDTO;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.dtos.responses.BestSellerDTO;
import com.amalitech.smartshop.dtos.responses.OrderItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.RevenueReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for order-related business operations.
 */
public interface OrderService {

    /**
     * Create a new order.
     *
     * @param addOrderDTO the order data
     * @return the created order response
     */
    OrderResponseDTO createOrder(AddOrderDTO addOrderDTO);

    /**
     * Get all orders with pagination.
     *
     * @param pageable pagination information
     * @return a page of order responses
     */
    Page<OrderResponseDTO> getAllOrders(Pageable pageable);

    /**
     * Get orders for a specific user with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return a page of order responses
     */
    Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable);

    /**
     * Get an order by its ID.
     *
     * @param id the order ID
     * @return the order response
     */
    OrderResponseDTO getOrderById(Long id);

    /**
     * Update an order's status.
     *
     * @param id the order ID
     * @param updateOrderDTO the update data
     * @return the updated order response
     */
    OrderResponseDTO updateOrderStatus(Long id, UpdateOrderDTO updateOrderDTO);

    /**
     * Delete an order and its items.
     *
     * @param id the order ID to delete
     */
    void deleteOrder(Long id);

    /**
     * Get items for a specific order.
     *
     * @param orderId the order ID
     * @return list of order item responses
     */
    List<OrderItemResponseDTO> getOrderItems(Long orderId);

    /**
     * Delete all items for an order.
     *
     * @param orderId the order ID
     */
    void deleteOrderItems(Long orderId);

    /**
     * Get high-value orders above a minimum amount.
     *
     * @param minAmount the minimum total amount
     * @param pageable pagination information
     * @return a page of high-value orders
     */
    Page<OrderResponseDTO> getHighValueOrders(Double minAmount, Pageable pageable);

    /**
     * Get best-selling products.
     *
     * @param limit maximum number of results
     * @return list of best-selling products
     */
    List<BestSellerDTO> getBestSellingProducts(Integer limit);

    /**
     * Get revenue report for a date range.
     *
     * @param startDate start date (yyyy-MM-dd)
     * @param endDate end date (yyyy-MM-dd)
     * @return list of daily revenue entries
     */
    List<RevenueReportDTO> getRevenueReport(String startDate, String endDate);
}
