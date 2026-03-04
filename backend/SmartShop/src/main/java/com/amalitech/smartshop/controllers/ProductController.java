package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import com.amalitech.smartshop.dtos.responses.ProductStatisticsDTO;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.interfaces.ProductService;
import com.amalitech.smartshop.utils.sorting.SortingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for product management operations.
 * Handles CRUD operations for products and product listings.
 */
@Tag(name = "Products", description = "APIs for managing products")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SortingService sortingService;

    @Operation(summary = "Add a new product", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addProduct(
            @Valid @RequestBody AddProductDTO request,
            @AuthenticationPrincipal User currentUser) {
        ProductResponseDTO product = productService.addProduct(request, currentUser.getId(), currentUser.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Product added successfully", product));
    }

    @Operation(summary = "Add multiple products", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> addProducts(
            @Valid @RequestBody List<AddProductDTO> requests,
            @AuthenticationPrincipal User currentUser) {
        List<ProductResponseDTO> products = requests.stream()
                .map(dto -> productService.addProduct(dto, currentUser.getId(), currentUser.getRole().name()))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        products.size() + " products added successfully", products));
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDTO>>> getAllProducts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "vendorId", required = false) Long vendorId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "inStock", required = false) Boolean inStock,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "ascending", defaultValue = "true") boolean ascending,
            @RequestParam(value = "algorithm", defaultValue = "QUICKSORT") String algorithm) {

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ProductResponseDTO> products;

        String userRole = currentUser != null ? currentUser.getRole().name() : null;
        Long userId = currentUser != null ? currentUser.getId() : null;
        boolean isAdmin = "ADMIN".equals(userRole);

        if ("VENDOR".equals(userRole) && vendorId == null) {
            vendorId = userId;
        }

        if (search != null || minPrice != null || maxPrice != null || inStock != null || categoryId != null) {
            products = productService.searchProducts(search, categoryId, minPrice, maxPrice, inStock, pageable);
        } else if (vendorId != null) {
            products = productService.getProductsByVendor(vendorId, pageable);
        } else {
            products = productService.getAllProducts(pageable, isAdmin);
        }

        List<ProductResponseDTO> productList = new ArrayList<>(products.getContent());

        if (sortBy != null) {
            try {
                SortingService.ProductSortField field = SortingService.ProductSortField.valueOf(sortBy.toUpperCase());
                SortingService.SortAlgorithm algo = SortingService.SortAlgorithm.valueOf(algorithm.toUpperCase());
                sortingService.sortProducts(productList, field, ascending, algo);
            } catch (IllegalArgumentException ignored) {
            }
        }

        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                productList,
                products.getNumber(),
                (int) products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Product fetched successfully", product));
    }

    @Operation(summary = "Update a product", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDTO request) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Product updated successfully", updatedProduct));
    }

    @Operation(summary = "Delete a product", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Product deleted successfully", null));
    }

    @Operation(summary = "Get products by category with optimized join fetch")
    @GetMapping("/category/{categoryId}/optimized")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDTO>>> getProductsByCategoryOptimized(
            @PathVariable Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ProductResponseDTO> products = productService.getProductsByCategoryWithInventory(categoryId, pageable);
        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                products.getContent(),
                products.getNumber(),
                (int) products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse));
    }

    @Operation(summary = "Get product statistics", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<ProductStatisticsDTO>>> getProductStatistics() {
        List<ProductStatisticsDTO> statistics = productService.getProductStatistics();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Product statistics fetched successfully", statistics));
    }
}
