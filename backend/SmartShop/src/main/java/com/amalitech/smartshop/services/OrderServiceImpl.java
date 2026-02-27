package com.amalitech.smartshop.services;

import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.OrderItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateOrderDTO;
import com.amalitech.smartshop.dtos.responses.OrderItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.entities.Inventory;
import com.amalitech.smartshop.entities.Order;
import com.amalitech.smartshop.entities.OrderItem;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.enums.OrderStatus;
import com.amalitech.smartshop.exceptions.ConstraintViolationException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the OrderService interface.
 * Handles all order-related business logic including order creation,
 * status updates, and inventory management.
 */
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
    private final CacheManager cacheManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponseDTO createOrder(AddOrderDTO addOrderDTO) {
        // I create an order and deduct inventory quantities within a single transaction
        // The transaction rolls back automatically on any exception
        log.info("Creating order for user: {}", addOrderDTO.getUserId());
        
        userRepository.findById(addOrderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + addOrderDTO.getUserId()));

        List<OrderItem> orderItems = new ArrayList<>();
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemDTO itemDTO : addOrderDTO.getItems()) {
            Product product = validateAndGetProduct(itemDTO.getProductId());
            Inventory inventory = validateAndReserveInventory(product, itemDTO.getQuantity());
            
            inventoriesToUpdate.add(inventory);
            invalidateProductCache(product.getId());

            double itemTotal = product.getPrice() * itemDTO.getQuantity();
            totalAmount += itemTotal;

            orderItems.add(OrderItem.builder()
                    .productId(product.getId())
                    .quantity(itemDTO.getQuantity())
                    .totalPrice(itemTotal)
                    .build());
        }

        inventoryRepository.saveAll(inventoriesToUpdate);

        Order order = Order.builder()
                .userId(addOrderDTO.getUserId())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getId());
        }
        orderItemRepository.saveAll(orderItems);

        log.info("Order created successfully with id: {}", savedOrder.getId());
        // Re-fetch with entity graph so items, item products, and user are loaded for the response
        Order fullOrder = orderRepository.findById(savedOrder.getId()).orElse(savedOrder);
        return buildOrderResponse(fullOrder);
    }

    @Override
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        // findAll uses @EntityGraph — items, item products, and user are JOIN FETCHed; no N+1
        return orderRepository.findAll(pageable).map(order ->
                cacheManager.get("ord:" + order.getId(), () -> buildOrderResponse(order))
        );
    }

    @Override
    public Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // findByUserId uses @EntityGraph — no N+1
        return orderRepository.findByUserId(userId, pageable).map(order ->
                cacheManager.get("ord:" + order.getId(), () -> buildOrderResponse(order))
        );
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        // findById uses @EntityGraph — items, item products, and user are JOIN FETCHed
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponseDTO updateOrderStatus(Long id, UpdateOrderDTO updateOrderDTO) {
        // findById uses @EntityGraph — associations are pre-loaded
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        if (updateOrderDTO.getStatus() != null) {
            order.setStatus(updateOrderDTO.getStatus());
        }

        Order updatedOrder = orderRepository.save(order);
        cacheManager.invalidate("ord:" + id);

        log.info("Order status updated successfully: {}", id);
        return buildOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        // I delete an order and its items within a transaction
        log.info("Deleting order: {}", id);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));

        try {
            // items are pre-loaded via entity graph on findById — no extra query needed
            List<OrderItem> loadedItems = order.getItems() != null ? order.getItems() : List.of();
            orderItemRepository.deleteAll(loadedItems);
            orderRepository.delete(order);

            cacheManager.invalidate("ord:" + id);
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
        // I validate inventory availability and reserve the requested quantity
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product '" + product.getName() + "' is out of stock"));

        if (inventory.getQuantity() < requestedQuantity) {
            throw new IllegalArgumentException("Insufficient stock for product '" + product.getName() + "'. Available: " + inventory.getQuantity() + ", Requested: " + requestedQuantity);
        }

        inventory.setQuantity(inventory.getQuantity() - requestedQuantity);
        return inventory;
    }

    private void invalidateProductCache(Long productId) {
        cacheManager.invalidate("prod:" + productId);
        cacheManager.invalidate("invent:" + productId);
        cacheManager.invalidate("invent:" + productId);
    }

    /** Builds the order response using associations pre-loaded by the entity graph — zero extra queries. */
    private OrderResponseDTO buildOrderResponse(Order order) {
        OrderResponseDTO response = orderMapper.toResponseDTO(order);

        // user is JOIN FETCHed via entity graph
        if (order.getUser() != null) {
            response.setUserName(order.getUser().getFullName());
        }

        // items and items.product are JOIN FETCHed via entity graph
        List<OrderItemResponseDTO> itemResponses = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                        .map(item -> {
                            OrderItemResponseDTO itemResponse = orderMapper.toOrderItemResponseDTO(item);
                            if (item.getProduct() != null) {
                                itemResponse.setProductName(item.getProduct().getName());
                            }
                            return itemResponse;
                        })
                        .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}
