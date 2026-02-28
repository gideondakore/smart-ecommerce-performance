package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.config.RequiresRole;
import com.amalitech.smartshop.dtos.requests.AddCartItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCartItemDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.CartResponseDTO;
import com.amalitech.smartshop.dtos.responses.CartSummaryDTO;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shopping cart operations.
 * Handles cart management and checkout.
 */
@Tag(name = "Cart Management", description = "APIs for managing shopping cart")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get user's cart")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartResponseDTO cart = cartService.getCartByUserId(userId);
        ApiResponse<CartResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart fetched successfully", cart);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Add item to cart")
    @PostMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> addItemToCart(
            @Valid @RequestBody AddCartItemDTO request,
            HttpServletRequest httpRequest) {
        // I add an item to the user's cart
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartResponseDTO cart = cartService.addItemToCart(request, userId);
        ApiResponse<CartResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Item added to cart successfully", cart);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemDTO request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartResponseDTO cart = cartService.updateCartItem(itemId, request, userId);
        ApiResponse<CartResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart item updated successfully", cart);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeItemFromCart(
            @PathVariable Long itemId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartResponseDTO cart = cartService.removeItemFromCart(itemId, userId);
        ApiResponse<CartResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Item removed from cart successfully", cart);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        cartService.clearCart(userId);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart cleared successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Checkout cart")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CartResponseDTO>> checkoutCart(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartResponseDTO cart = cartService.checkoutCart(userId);
        ApiResponse<CartResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart checked out successfully", cart);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get cart summary with totals")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> getCartSummary(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartSummaryDTO summary = cartService.getCartSummary(userId);
        ApiResponse<CartSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart summary fetched successfully", summary);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update cart item quantity by product ID")
    @PatchMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateCartItemByProduct(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        CartResponseDTO cart = cartService.updateCartItemByProduct(productId, quantity, userId);
        ApiResponse<CartResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart item quantity updated successfully", cart);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Check if cart exists for user")
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCartExists(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        boolean exists = cartService.cartExists(userId);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart existence checked", exists);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete cart by user")
    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<Void>> deleteCartByUser(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authUserId");
        cartService.deleteCart(userId);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Cart deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }
}
