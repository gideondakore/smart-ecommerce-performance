package com.amalitech.smartshop.services;

import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.exceptions.ConstraintViolationException;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.ProductService;
import com.amalitech.smartshop.mappers.ProductMapper;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of the ProductService interface.
 * Handles all product-related business logic including CRUD operations
 * and inventory management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductJpaRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryJpaRepository categoryRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final CacheManager cacheManager;

    @Override
    public ProductResponseDTO addProduct(AddProductDTO addProductDTO, Long userId, String userRole) {

        if (productRepository.existsByNameIgnoreCase(addProductDTO.getName())) {
            throw new ResourceAlreadyExistsException("Product already exists");
        }
        
        Category category = categoryRepository.findById(addProductDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + addProductDTO.getCategoryId()));
        
        Product product = productMapper.toEntity(addProductDTO);
        product.setCategoryId(addProductDTO.getCategoryId());
        
        if ("VENDOR".equals(userRole) && userId != null) {
            product.setVendorId(userId);
        }

        if(product.getImageUrl() == null){
            product.setImageUrl("https://placehold.net/1.png");
        }

        Product savedProduct = productRepository.save(product);
        
        ProductResponseDTO response = productMapper.toResponseDTO(savedProduct);
        response.setCategoryName(category.getName());
        
        inventoryRepository.findByProductId(savedProduct.getId())
                .ifPresent(inventory -> response.setQuantity(inventory.getQuantity()));

        return response;
    }

    @Override
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, boolean isAdmin) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return mapProductPageToResponse(productPage);
    }

    @Override
    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable, boolean isAdmin) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        return mapProductPageToResponse(productPage);
    }

    @Override
    public Page<ProductResponseDTO> getProductsByVendor(Long vendorId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByVendorId(vendorId, pageable);
        return mapProductPageToResponse(productPage);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        // findById is annotated with @EntityGraph — inventory and category are JOIN FETCHed
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        ProductResponseDTO response = productMapper.toResponseDTO(product);
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getInventory() != null) {
            response.setQuantity(product.getInventory().getQuantity());
        }
        return response;
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        validateProductNameUniqueness(existingProduct, updateProductDTO.getName());
        
        if (updateProductDTO.getCategoryId() != null) {
            categoryRepository.findById(updateProductDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + updateProductDTO.getCategoryId()));
            existingProduct.setCategoryId(updateProductDTO.getCategoryId());
        }

        productMapper.updateEntity(updateProductDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        invalidateProductCache(id);

        ProductResponseDTO response = productMapper.toResponseDTO(updatedProduct);
        enrichProductResponse(response, updatedProduct);

        log.info("Product updated successfully: {}", id);
        return response;
    }

    @Override
    public List<ProductResponseDTO> getAllProductsList() {
        // findAllWithInventory() uses LEFT JOIN FETCH for both inventory and category — single query
        return productRepository.findAllWithInventory().stream()
                .map(this::toResponseWithAssociations)
                .toList();
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        try {
            productRepository.delete(product);
            invalidateProductCache(id);
            log.info("Product deleted successfully: {}", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
                throw new ConstraintViolationException(
                        "Cannot delete product. It is being used in orders or inventory. Please remove related records first.");
            }
            throw ex;
        }
    }

    private Page<ProductResponseDTO> mapProductPageToResponse(Page<Product> productPage) {
        // All products in the page already have inventory and category JOIN FETCHed — no extra queries
        return productPage.map(this::toResponseWithAssociations);
    }

    /** Maps a product to a DTO using already-loaded associations — never triggers lazy loads. */
    private ProductResponseDTO toResponseWithAssociations(Product product) {
        ProductResponseDTO response = productMapper.toResponseDTO(product);
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getInventory() != null) {
            response.setQuantity(product.getInventory().getQuantity());
        }
        return response;
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(String search, Long categoryId, Double minPrice, Double maxPrice, Boolean inStock, Pageable pageable) {
        // I search and filter products based on the given criteria using JPA Specifications
        log.info("Searching products with search={}, categoryId={}, minPrice={}, maxPrice={}, inStock={}", 
                search, categoryId, minPrice, maxPrice, inStock);
        
        // Build specification from search parameters
        Specification<Product> spec = ProductSpecification.buildSpecification(
                search,
                categoryId,
                null, // categoryName
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                null, // vendorId
                null, // featured
                null, // active
                inStock
        );
        
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return mapProductPageToResponse(productPage);
    }

    private void validateProductNameUniqueness(Product existingProduct, String newName) {
        if (newName != null
                && !existingProduct.getName().equalsIgnoreCase(newName)
                && productRepository.existsByNameIgnoreCase(newName)) {
            throw new ResourceAlreadyExistsException("Product with name '" + newName + "' already exists");
        }
    }

    private void enrichProductResponse(ProductResponseDTO response, Product product) {
        // product was loaded with @EntityGraph — associations are already fetched
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getInventory() != null) {
            response.setQuantity(product.getInventory().getQuantity());
        }
    }

    private void invalidateProductCache(Long productId) {
        cacheManager.invalidate("prod:" + productId);
        cacheManager.invalidate("invent:" + productId);
    }
}
