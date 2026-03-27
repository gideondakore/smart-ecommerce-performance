package com.amalitech.smartshop.services;

import com.amalitech.smartshop.async.AsyncAggregationService;
import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.OrderItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateOrderDTO;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.entities.*;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.repositories.jpa.*;
import com.amalitech.smartshop.enums.OrderStatus;
import com.amalitech.smartshop.mappers.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderServiceImpl orderService;

    @Mock
    private OrderJpaRepository orderRepository;

    @Mock
    private OrderItemJpaRepository orderItemRepository;

    @Mock
    private ProductJpaRepository productRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private InventoryJpaRepository inventoryRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private AsyncAggregationService asyncAggregationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, productRepository,
                                       userRepository, inventoryRepository, orderMapper, asyncAggregationService);
    }

    @Test
    void createOrder_Success() {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(1L);

        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(2);
        dto.setItems(List.of(itemDTO));

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(999.99);
        product.setAvailable(true);

        Inventory inventory = new Inventory();
        inventory.setQuantity(10);

        Order savedOrder = new Order();
        savedOrder.setId(1L);

        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.decrementStock(1L, 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepository.findById(savedOrder.getId())).thenReturn(Optional.of(savedOrder));
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.createOrder(dto);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_UserNotFound() {
        AddOrderDTO dto = new AddOrderDTO();
        dto.setUserId(1L);
        dto.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void getOrderById_Success() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);

        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponseDTO(order)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertNotNull(result);
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void updateOrderStatus_Success() {
        UpdateOrderDTO updateDTO = new UpdateOrderDTO();
        updateDTO.setStatus(OrderStatus.SHIPPED);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setUser(user);
        existingOrder.setStatus(OrderStatus.PENDING);

        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setUser(user);
        updatedOrder.setStatus(OrderStatus.SHIPPED);

        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(updatedOrder);
        when(orderMapper.toResponseDTO(updatedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, updateDTO);

        assertNotNull(result);
    }

    @Test
    void deleteOrder_Success() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> orderService.deleteOrder(1L));
        verify(orderRepository).delete(order);
    }
}
