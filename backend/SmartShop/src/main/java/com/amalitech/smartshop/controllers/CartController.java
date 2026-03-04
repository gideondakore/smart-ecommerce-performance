package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.AddCartItemDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCartItemDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.CartResponseDTO;
import com.amalitech.smartshop.dtos.responses.CartSummaryDTO;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.interfaces.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shopping cart operations.
 * Handles cart management and checkout.
 */
@Tag(name = "Cart", description = "APIs for managing shopping cart")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get user's cart")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(@AuthenticationPrincipal User currentUser) {
        CartResponseDTO cart = cartService.getCartByUserId(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart fetched successfully", cart));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> addItemToCart(
            @Valid @RequestBody AddCartItemDTO request,
            @AuthenticationPrincipal User currentUser) {
        CartResponseDTO cart = cartService.addItemToCart(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Item added to cart successfully", cart));
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemDTO request,
            @AuthenticationPrincipal User currentUser) {
        CartResponseDTO cart = cartService.updateCartItem(itemId, request, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart item updated successfully", cart));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeItemFromCart(
            @PathVariable Long itemId,
            @AuthenticationPrincipal User currentUser) {
        CartResponseDTO cart = cartService.removeItemFromCart(itemId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Item removed from cart successfully", cart));
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart cleared successfully", null));
    }

    @Operation(summary = "Checkout cart")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CartResponseDTO>> checkoutCart(@AuthenticationPrincipal User currentUser) {
        CartResponseDTO cart = cartService.checkoutCart(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart checked out successfully", cart));
    }

    @Operation(summary = "Get cart summary with totals")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CartSummaryDTO>> getCartSummary(@AuthenticationPrincipal User currentUser) {
        CartSummaryDTO summary = cartService.getCartSummary(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart summary fetched successfully", summary));
    }

    @Operation(summary = "Update cart item quantity by product ID")
    @PatchMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateCartItemByProduct(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal User currentUser) {
        CartResponseDTO cart = cartService.updateCartItemByProduct(productId, quantity, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart item quantity updated successfully", cart));
    }

    @Operation(summary = "Check if cart exists for user")
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCartExists(@AuthenticationPrincipal User currentUser) {
        boolean exists = cartService.cartExists(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart existence checked", exists));
    }

    @Operation(summary = "Delete cart by user")
    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<Void>> deleteCartByUser(@AuthenticationPrincipal User currentUser) {
        cartService.deleteCart(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cart deleted successfully", null));
    }
}
