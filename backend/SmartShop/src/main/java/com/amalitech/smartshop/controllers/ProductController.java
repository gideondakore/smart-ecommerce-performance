package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.config.RequiresRole;
import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import com.amalitech.smartshop.dtos.responses.ProductStatisticsDTO;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.ProductService;
import com.amalitech.smartshop.utils.sorting.SortingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for product management operations.
 * Handles CRUD operations for products and product listings.
 */
@Tag(name = "Product Management", description = "APIs for managing products")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SortingService sortingService;

    @Operation(summary = "Add a new product")
    @RequiresRole({UserRole.ADMIN, UserRole.VENDOR})
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addProduct(
            @Valid @RequestBody AddProductDTO request,
            @RequestAttribute(value = "authUserId", required = false) Long userId,
            @RequestAttribute(value = "authenticatedUserRole", required = false) String userRole) {
        ProductResponseDTO product = productService.addProduct(request, userId, userRole);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product added successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Add multiple products")
    @RequiresRole({UserRole.ADMIN, UserRole.VENDOR})
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> addProducts(
            @Valid @RequestBody List<AddProductDTO> requests,
            @RequestAttribute(value = "authUserId", required = false) Long userId,
            @RequestAttribute(value = "authenticatedUserRole", required = false) String userRole) {
        List<ProductResponseDTO> products = requests.stream()
                .map(dto -> productService.addProduct(dto, userId, userRole))
                .toList();
        ApiResponse<List<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(),
                products.size() + " products added successfully", products);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponseDTO>>> getAllProducts(
            @RequestAttribute(value = "authenticatedUserRole", required = false) String userRole,
            @RequestAttribute(value = "authUserId", required = false) Long userId,
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
            @RequestParam(value = "algorithm", defaultValue = "QUICKSORT") String algorithm
    ) {
        // I retrieve products with optional filtering and search criteria
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ProductResponseDTO> products;

        boolean isAdmin = "ADMIN".equals(userRole);

        // If user is VENDOR and no vendorId specified, show only their products
        if ("VENDOR".equals(userRole) && vendorId == null) {
            vendorId = userId;
        }

        // I use the search method when any search/filter criteria is provided
        if (search != null || minPrice != null || maxPrice != null || inStock != null || categoryId != null) {
            products = productService.searchProducts(search, categoryId, minPrice, maxPrice, inStock, pageable);
        } else if (vendorId != null) {
            products = productService.getProductsByVendor(vendorId, pageable);
        } else {
            products = productService.getAllProducts(pageable, isAdmin);
        }

        List<ProductResponseDTO> productList = new ArrayList<>(products.getContent());

        // I apply custom sorting if sortBy is specified
        if (sortBy != null) {
            try {
                SortingService.ProductSortField field = SortingService.ProductSortField.valueOf(sortBy.toUpperCase());
                SortingService.SortAlgorithm algo = SortingService.SortAlgorithm.valueOf(algorithm.toUpperCase());
                sortingService.sortProducts(productList, field, ascending, algo);
            } catch (IllegalArgumentException e) {
                // I ignore invalid sortBy or algorithm values
            }
        }

        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                productList,
                products.getNumber(),
                (int) products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
        ApiResponse<PagedResponse<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product fetched successfully", product);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update a product")
    @RequiresRole({UserRole.ADMIN, UserRole.VENDOR})
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDTO request) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, request);
        ApiResponse<ProductResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product updated successfully", updatedProduct);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a product")
    @RequiresRole({UserRole.ADMIN, UserRole.VENDOR})
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
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
        ApiResponse<PagedResponse<ProductResponseDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get product statistics")
    @RequiresRole(UserRole.ADMIN)
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<ProductStatisticsDTO>>> getProductStatistics() {
        List<ProductStatisticsDTO> statistics = productService.getProductStatistics();
        ApiResponse<List<ProductStatisticsDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Product statistics fetched successfully", statistics);
        return ResponseEntity.ok(apiResponse);
    }
}
