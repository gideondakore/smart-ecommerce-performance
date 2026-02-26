package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for product-related business operations.
 */
public interface ProductService {

    /**
     * Add a new product.
     *
     * @param addProductDTO the product data
     * @param userId the authenticated user ID
     * @param userRole the authenticated user role
     * @return the created product response
     */
    ProductResponseDTO addProduct(AddProductDTO addProductDTO, Long userId, String userRole);

    /**
     * Get all products with pagination.
     *
     * @param pageable pagination information
     * @param isAdmin whether the request is from an admin user
     * @return a page of product responses
     */
    Page<ProductResponseDTO> getAllProducts(Pageable pageable, boolean isAdmin);

    /**
     * Get products by category with pagination.
     *
     * @param categoryId the category ID
     * @param pageable pagination information
     * @param isAdmin whether the request is from an admin user
     * @return a page of product responses
     */
    Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable, boolean isAdmin);

    /**
     * Get products by vendor with pagination.
     *
     * @param vendorId the vendor ID
     * @param pageable pagination information
     * @return a page of product responses
     */
    Page<ProductResponseDTO> getProductsByVendor(Long vendorId, Pageable pageable);

    /**
     * Get a product by its ID.
     *
     * @param id the product ID
     * @return the product response
     */
    ProductResponseDTO getProductById(Long id);

    /**
     * Update a product.
     *
     * @param id the product ID
     * @param updateProductDTO the update data
     * @return the updated product response
     */
    ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO);

    /**
     * Get all products as a list.
     *
     * @return list of all product responses
     */
    List<ProductResponseDTO> getAllProductsList();

    /**
     * Delete a product.
     *
     * @param id the product ID to delete
     */
    void deleteProduct(Long id);

    /**
     * Search and filter products with multiple criteria.
     *
     * @param search search term for name and description
     * @param categoryId filter by category ID
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param inStock filter by availability
     * @param pageable pagination information
     * @return a page of product responses matching the criteria
     */
    Page<ProductResponseDTO> searchProducts(String search, Long categoryId, Double minPrice, Double maxPrice, Boolean inStock, Pageable pageable);
}
