package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddCartItemDTO;
import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.OrderItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCartItemDTO;
import com.amalitech.smartshop.dtos.responses.CartItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.CartResponseDTO;
import com.amalitech.smartshop.entities.Cart;
import com.amalitech.smartshop.entities.CartItem;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.interfaces.CartService;
import com.amalitech.smartshop.interfaces.OrderService;
import com.amalitech.smartshop.repositories.jpa.CartItemJpaRepository;
import com.amalitech.smartshop.repositories.jpa.CartJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartJpaRepository cartRepository;
    private final CartItemJpaRepository cartItemRepository;
    private final ProductJpaRepository productRepository;
    private final UserJpaRepository userRepository;
    private final OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByUserId(Long userId) {
        log.info("Getting cart for user: {}", userId);

        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO addItemToCart(AddCartItemDTO request, Long userId) {
        log.info("Adding item to cart for user: {}", userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Cart cart = getOrCreateCart(userId);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        var existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);

        log.info("Item added to cart successfully");
        Cart freshCart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateCartItem(Long itemId, UpdateCartItemDTO request, Long userId) {
        log.info("Updating cart item: {} for user: {}", itemId, userId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));

        Cart cart = cartItem.getCart();
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        if (!cart.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update items in your own cart");
        }

        cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found in cart"))
                .setQuantity(request.getQuantity());

        cartRepository.save(cart);

        log.info("Cart item updated successfully");
        Cart freshCart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItemFromCart(Long itemId, Long userId) {
        log.info("Removing item from cart: {} for user: {}", itemId, userId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));

        Cart cart = cartItem.getCart();
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        if (!cart.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only remove items from your own cart");
        }

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);

        log.info("Item removed from cart successfully");
        Cart freshCart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        cartItemRepository.deleteByCart_Id(cart.getId());
        log.info("Cart cleared successfully");
    }

    @Override
    @Transactional
    public CartResponseDTO checkoutCart(Long userId) {
        log.info("Checking out cart for user: {}", userId);

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty cart");
        }

        AddOrderDTO orderDTO = new AddOrderDTO();
        orderDTO.setUserId(userId);
        List<OrderItemDTO> orderItems = cartItems.stream()
                .map(item -> {
                    OrderItemDTO orderItem = new OrderItemDTO();
                    orderItem.setProductId(item.getProduct().getId());
                    orderItem.setQuantity(item.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());
        orderDTO.setItems(orderItems);

        orderService.createOrder(orderDTO);
        clearCart(userId);

        log.info("Cart checked out successfully");
        Cart freshCart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponseDTO buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cart.getItems() != null ? cart.getItems() : List.of();

        List<CartItemResponseDTO> items = new ArrayList<>();
        double totalAmount = 0.0;
        int totalItems = 0;

        for (CartItem item : cartItems) {
            if (item.getProduct() != null) {
                double itemTotal = item.getProduct().getPrice() * item.getQuantity();
                items.add(CartItemResponseDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(itemTotal)
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build());
                totalAmount += itemTotal;
                totalItems += item.getQuantity();
            }
        }

        return CartResponseDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
