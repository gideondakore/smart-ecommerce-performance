package com.amalitech.smartshop.services;

import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.dtos.requests.AddCategoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCategoryDTO;
import com.amalitech.smartshop.dtos.responses.CategoryResponseDTO;
import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.CategoryService;
import com.amalitech.smartshop.mappers.CategoryMapper;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the CategoryService interface.
 * Handles all category-related business logic including CRUD operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryJpaRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CacheManager cacheManager;
    private final ProductJpaRepository productRepository;
    private final InventoryJpaRepository inventoryRepository;

    @Override
    public CategoryResponseDTO addCategory(AddCategoryDTO addCategoryDTO) {
        log.info("Adding new category: {}", addCategoryDTO.getName());
        
        if (categoryRepository.existsByNameIgnoreCase(addCategoryDTO.getName())) {
            throw new ResourceAlreadyExistsException("Category with name '" + addCategoryDTO.getName() + "' already exists");
        }
        
        Category category = categoryMapper.toEntity(addCategoryDTO);
        Category savedCategory = categoryRepository.save(category);
        
        log.info("Category added successfully with id: {}", savedCategory.getId());
        return categoryMapper.toResponseDTO(savedCategory);
    }

    @Override
    public Page<CategoryResponseDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(category -> {
            CategoryResponseDTO dto = categoryMapper.toResponseDTO(category);
            cacheManager.get("cat:" + category.getId(), () -> category);
            return dto;
        });
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toResponseDTO(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
        log.info("Updating category: {}", id);
        
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        validateCategoryNameUniqueness(existingCategory, updateCategoryDTO.getName());

        categoryMapper.updateEntity(updateCategoryDTO, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);

        cacheManager.invalidate("cat:" + id);

        log.info("Category updated successfully: {}", id);
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Delete all products in this category and their inventory
        List<Product> products = productRepository.findByCategoryId(id);
        for (Product product : products) {
            deleteProductInventory(product);
            productRepository.delete(product);
            cacheManager.invalidate("prod:" + product.getId());
        }

        categoryRepository.delete(category);
        cacheManager.invalidate("cat:" + id);
        
        log.info("Category deleted successfully: {}", id);
    }

    private void validateCategoryNameUniqueness(Category existingCategory, String newName) {
        if (newName != null
                && !existingCategory.getName().equalsIgnoreCase(newName)
                && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new ResourceAlreadyExistsException("Category with name '" + newName + "' already exists");
        }
    }

    private void deleteProductInventory(Product product) {
        inventoryRepository.findByProductId(product.getId())
                .ifPresent(inventory -> {
                    inventoryRepository.delete(inventory);
                    cacheManager.invalidate("invent:" + inventory.getId());
                    cacheManager.invalidate("invent:" + product.getId());
                    cacheManager.invalidate("invent:" + product.getId());
                });
    }
}
