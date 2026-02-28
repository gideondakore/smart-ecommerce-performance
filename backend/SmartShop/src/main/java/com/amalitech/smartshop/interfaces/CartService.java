package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.dtos.requests.AddCartItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCartItemDTO;
import com.amalitech.smartshop.dtos.responses.CartResponseDTO;
import com.amalitech.smartshop.dtos.responses.CartSummaryDTO;

public interface CartService {
    CartResponseDTO getCartByUserId(Long userId);
    CartResponseDTO addItemToCart(AddCartItemDTO request, Long userId);
    CartResponseDTO updateCartItem(Long itemId, UpdateCartItemDTO request, Long userId);
    CartResponseDTO removeItemFromCart(Long itemId, Long userId);
    void clearCart(Long userId);
    CartResponseDTO checkoutCart(Long userId);
    CartResponseDTO updateCartItemByProduct(Long productId, Integer quantity, Long userId);
    CartResponseDTO removeItemByProduct(Long productId, Long userId);
    boolean cartExists(Long userId);
    void deleteCart(Long userId);
    CartSummaryDTO getCartSummary(Long userId);
}
