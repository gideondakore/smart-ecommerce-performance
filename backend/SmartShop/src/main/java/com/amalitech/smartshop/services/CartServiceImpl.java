package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddCartItemDTO;
import com.amalitech.smartshop.dtos.requests.AddOrderDTO;
import com.amalitech.smartshop.dtos.requests.OrderItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCartItemDTO;
import com.amalitech.smartshop.dtos.responses.CartItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.CartResponseDTO;
import com.amalitech.smartshop.entities.Cart;
import com.amalitech.smartshop.entities.CartItem;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.interfaces.CartService;
import com.amalitech.smartshop.interfaces.OrderService;
import com.amalitech.smartshop.repositories.jpa.CartItemJpaRepository;
import com.amalitech.smartshop.repositories.jpa.CartJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
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
    private final OrderService orderService;

    @Override
    public CartResponseDTO getCartByUserId(Long userId) {
        log.info("Getting cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
        
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO addItemToCart(AddCartItemDTO request, Long userId) {
        log.info("Adding item to cart for user: {}", userId);
        
        productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
        
        // Ensure items list is mutable before modifying
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Check if item already exists in cart
        // We operate on cart.getItems() (the managed collection) so that the in-memory
        // state is always in sync before cartRepository.save(cart) runs.
        // Calling cartItemRepository.save() then cartRepository.save(cart) would trigger
        // orphanRemoval and DELETE the just-inserted item because cart.items is stale.
        var existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cartId(cart.getId())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        // Single save — cascades to items and updates the cart timestamp via @UpdateTimestamp
        cartRepository.save(cart);
        
        log.info("Item added to cart successfully");
        // Re-fetch with entity graph to get up-to-date items with products loaded
        Cart freshCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateCartItem(Long itemId, UpdateCartItemDTO request, Long userId) {
        log.info("Updating cart item: {} for user: {}", itemId, userId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));
        
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        if (!cart.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only update items in your own cart");
        }

        // Update the item directly within the managed cart.items collection.
        // Updating a separate cartItem instance and then calling cartRepository.save(cart)
        // would let cascade overwrite the quantity with the stale value from cart.items.
        cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found in cart"))
                .setQuantity(request.getQuantity());

        // Single save — cascades to items and updates the cart timestamp via @UpdateTimestamp
        cartRepository.save(cart);
        
        log.info("Cart item updated successfully");
        // Re-fetch with entity graph to get up-to-date items with products loaded
        Cart freshCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItemFromCart(Long itemId, Long userId) {
        log.info("Removing item from cart: {} for user: {}", itemId, userId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));
        
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        if (!cart.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only remove items from your own cart");
        }

        // Remove from the cart's items collection; orphanRemoval=true handles the DB DELETE.
        // Do NOT call cartItemRepository.deleteById() here — doing so leaves a deleted entity
        // in the Hibernate session, which then throws ObjectDeletedException when
        // cartRepository.save(cart) cascades and tries to merge the same instance.
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);
        
        log.info("Item removed from cart successfully");
        // Re-fetch with entity graph to get up-to-date items with products loaded
        Cart freshCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        cartItemRepository.deleteByCartId(cart.getId());
        
        log.info("Cart cleared successfully");
    }

    @Override
    @Transactional
    public CartResponseDTO checkoutCart(Long userId) {
        log.info("Checking out cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty cart");
        }
        
        // Create order from cart items
        AddOrderDTO orderDTO = new AddOrderDTO();
        orderDTO.setUserId(userId);
        List<OrderItemDTO> orderItems = cartItems.stream()
                .map(item -> {
                    OrderItemDTO orderItem = new OrderItemDTO();
                    orderItem.setProductId(item.getProductId());
                    orderItem.setQuantity(item.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList());
        orderDTO.setItems(orderItems);
        
        orderService.createOrder(orderDTO);
        
        // Clear cart after successful checkout
        clearCart(userId);
        
        log.info("Cart checked out successfully");
        // Re-fetch with entity graph — items are now empty after clearCart
        Cart freshCart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return buildCartResponse(freshCart);
    }

    /** Builds the cart response using associations pre-loaded by the entity graph — zero extra queries. */
    private CartResponseDTO buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cart.getItems() != null ? cart.getItems() : List.of();
        
        List<CartItemResponseDTO> items = new ArrayList<>();
        double totalAmount = 0.0;
        int totalItems = 0;
        
        for (CartItem item : cartItems) {
            // product is JOIN FETCHed via entity graph — no extra query per item
            if (item.getProduct() != null) {
                double itemTotal = item.getProduct().getPrice() * item.getQuantity();
                items.add(CartItemResponseDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
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
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
