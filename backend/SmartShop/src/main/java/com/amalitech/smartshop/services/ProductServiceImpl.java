package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import com.amalitech.smartshop.dtos.responses.ProductStatisticsDTO;
import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.ConstraintViolationException;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.ProductService;
import com.amalitech.smartshop.mappers.ProductMapper;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductSpecification;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductJpaRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryJpaRepository categoryRepository;
    private final UserJpaRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public ProductResponseDTO addProduct(AddProductDTO addProductDTO, Long userId, String userRole) {
        if (productRepository.existsByNameIgnoreCase(addProductDTO.getName())) {
            throw new ResourceAlreadyExistsException("Product already exists");
        }

        Category category = categoryRepository.findById(addProductDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + addProductDTO.getCategoryId()));

        Product product = productMapper.toEntity(addProductDTO);
        product.setCategory(category);

        if ("VENDOR".equals(userRole) && userId != null) {
            User vendor = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + userId));
            product.setVendor(vendor);
        }

        if (product.getImageUrl() == null) {
            product.setImageUrl("https://placehold.net/1.png");
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponseDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, boolean isAdmin) {
        return productRepository.findAll(pageable).map(productMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable, boolean isAdmin) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return productRepository.findByCategory_Id(categoryId, pageable).map(productMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByVendor(Long vendorId, Pageable pageable) {
        return productRepository.findByVendor_Id(vendorId, pageable).map(productMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return productMapper.toResponseDTO(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public ProductResponseDTO updateProduct(Long id, UpdateProductDTO updateProductDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        validateProductNameUniqueness(existingProduct, updateProductDTO.getName());

        if (updateProductDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateProductDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + updateProductDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }

        productMapper.updateEntity(updateProductDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully: {}", id);
        return productMapper.toResponseDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProductsList() {
        return productRepository.findAllWithInventory().stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        try {
            productRepository.delete(product);
            log.info("Product deleted successfully: {}", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
                throw new ConstraintViolationException(
                        "Cannot delete product. It is being used in orders or inventory. Please remove related records first.");
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(String search, Long categoryId, Double minPrice, Double maxPrice, Boolean inStock, Pageable pageable) {
        log.info("Searching products with search={}, categoryId={}, minPrice={}, maxPrice={}, inStock={}",
                search, categoryId, minPrice, maxPrice, inStock);

        Specification<Product> spec = ProductSpecification.buildSpecification(
                search, categoryId, null,
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                null, null, null, inStock);

        return productRepository.findAll(spec, pageable).map(productMapper::toResponseDTO);
    }

    private void validateProductNameUniqueness(Product existingProduct, String newName) {
        if (newName != null
                && !existingProduct.getName().equalsIgnoreCase(newName)
                && productRepository.existsByNameIgnoreCase(newName)) {
            throw new ResourceAlreadyExistsException("Product with name '" + newName + "' already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCategoryWithInventory(Long categoryId, Pageable pageable) {
        log.info("Getting products by category {} with inventory via JPQL", categoryId);

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return productRepository.findByCategoryIdWithInventory(categoryId, pageable).map(productMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductStatisticsDTO> getProductStatistics() {
        log.info("Generating product statistics by category");

        List<Object[]> results = productRepository.getProductStatisticsByCategory();
        return results.stream().map(row -> ProductStatisticsDTO.builder()
                .categoryName((String) row[0])
                .productCount(((Number) row[1]).longValue())
                .averagePrice(((Number) row[2]).doubleValue())
                .build()
        ).toList();
    }
}
