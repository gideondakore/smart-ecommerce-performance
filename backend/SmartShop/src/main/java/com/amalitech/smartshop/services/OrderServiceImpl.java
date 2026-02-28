package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.OrderItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateOrderDTO;
import com.amalitech.smartshop.dtos.responses.BestSellerDTO;
import com.amalitech.smartshop.dtos.responses.OrderItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.dtos.responses.RevenueReportDTO;
import com.amalitech.smartshop.entities.Inventory;
import com.amalitech.smartshop.entities.Order;
import com.amalitech.smartshop.entities.OrderItem;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.enums.OrderStatus;
import com.amalitech.smartshop.exceptions.ConstraintViolationException;
import com.amalitech.smartshop.exceptions.InsufficientStockException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.OrderService;
import com.amalitech.smartshop.mappers.OrderMapper;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.OrderItemJpaRepository;
import com.amalitech.smartshop.repositories.jpa.OrderJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final ProductJpaRepository productRepository;
    private final UserJpaRepository userRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(AddOrderDTO addOrderDTO) {
        log.info("Creating order for user: {}", addOrderDTO.getUserId());

        User user = userRepository.findById(addOrderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + addOrderDTO.getUserId()));

        List<OrderItem> orderItems = new ArrayList<>();
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemDTO itemDTO : addOrderDTO.getItems()) {
            Product product = validateAndGetProduct(itemDTO.getProductId());
            Inventory inventory = validateAndReserveInventory(product, itemDTO.getQuantity());
            inventoriesToUpdate.add(inventory);

            double itemTotal = product.getPrice() * itemDTO.getQuantity();
            totalAmount += itemTotal;

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .totalPrice(itemTotal)
                    .build());
        }

        inventoryRepository.saveAll(inventoriesToUpdate);

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .items(orderItems)
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {}", savedOrder.getId());

        Order fullOrder = orderRepository.findById(savedOrder.getId()).orElse(savedOrder);
        return orderMapper.toResponseDTO(fullOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return orderRepository.findByUser_Id(userId, pageable).map(orderMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return orderMapper.toResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, UpdateOrderDTO updateOrderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        if (updateOrderDTO.getStatus() != null) {
            order.setStatus(updateOrderDTO.getStatus());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {}", id);
        return orderMapper.toResponseDTO(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        try {
            orderRepository.delete(order);
            log.info("Order deleted successfully: {}", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
                throw new ConstraintViolationException(
                        "Cannot delete order. It has related dependencies that must be removed first.");
            }
            throw ex;
        }
    }

    private Product validateAndGetProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product '" + product.getName() + "' is not available");
        }
        return product;
    }

    private Inventory validateAndReserveInventory(Product product, int requestedQuantity) {
        Inventory inventory = inventoryRepository.findByProduct_Id(product.getId())
                .orElseThrow(() -> new InsufficientStockException("Product '" + product.getName() + "' is out of stock"));

        if (inventory.getQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + product.getName()
                            + "'. Available: " + inventory.getQuantity()
                            + ", Requested: " + requestedQuantity);
        }

        inventory.setQuantity(inventory.getQuantity() - requestedQuantity);
        return inventory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponseDTO> getOrderItems(Long orderId) {
        log.info("Getting items for order: {}", orderId);

        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        return items.stream().map(item -> {
            OrderItemResponseDTO dto = new OrderItemResponseDTO();
            dto.setId(item.getId());
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setTotalPrice(item.getTotalPrice());
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public void deleteOrderItems(Long orderId) {
        log.info("Deleting items for order: {}", orderId);

        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        orderItemRepository.deleteByOrder_Id(orderId);
        log.info("Order items deleted successfully for order: {}", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getHighValueOrders(Double minAmount, Pageable pageable) {
        log.info("Finding high-value orders with minimum amount: {}", minAmount);
        return orderRepository.findHighValueOrders(minAmount, pageable).map(orderMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BestSellerDTO> getBestSellingProducts(Integer limit) {
        log.info("Finding top {} best-selling products", limit);

        List<Object[]> results = orderItemRepository.findBestSellingProducts(limit);
        return results.stream().map(row -> BestSellerDTO.builder()
                .productId(((Number) row[0]).longValue())
                .productName((String) row[1])
                .totalSold(((Number) row[2]).longValue())
                .totalRevenue(((Number) row[3]).doubleValue())
                .build()
        ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueReportDTO> getRevenueReport(String startDate, String endDate) {
        log.info("Generating revenue report from {} to {}", startDate, endDate);

        List<Object[]> results = orderRepository.getRevenueReport(startDate, endDate);
        return results.stream().map(row -> RevenueReportDTO.builder()
                .orderDate(((java.sql.Date) row[0]).toLocalDate())
                .orderCount(((Number) row[1]).longValue())
                .totalRevenue(((Number) row[2]).doubleValue())
                .build()
        ).toList();
    }
}
