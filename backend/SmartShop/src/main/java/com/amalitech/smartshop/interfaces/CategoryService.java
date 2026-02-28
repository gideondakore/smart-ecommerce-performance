package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.dtos.requests.AddCategoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCategoryDTO;
import com.amalitech.smartshop.dtos.responses.CategoryResponseDTO;
import com.amalitech.smartshop.dtos.responses.CategoryWithCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for category-related business operations.
 */
public interface CategoryService {

    /**
     * Add a new category.
     *
     * @param addCategoryDTO the category data
     * @return the created category response
     */
    CategoryResponseDTO addCategory(AddCategoryDTO addCategoryDTO);

    /**
     * Get all categories with pagination.
     *
     * @param pageable pagination information
     * @return a page of category responses
     */
    Page<CategoryResponseDTO> getAllCategories(Pageable pageable);

    /**
     * Get a category by its ID.
     *
     * @param id the category ID
     * @return the category response
     */
    CategoryResponseDTO getCategoryById(Long id);

    /**
     * Get a category by its name (case-insensitive).
     *
     * @param name the category name
     * @return the category response
     */
    CategoryResponseDTO getCategoryByName(String name);

    /**
     * Get all categories with their product counts via native SQL.
     *
     * @return list of categories with product counts
     */
    List<CategoryWithCountDTO> getCategoriesWithProductCount();

    /**
     * Update a category.
     *
     * @param id the category ID
     * @param updateCategoryDTO the update data
     * @return the updated category response
     */
    CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);

    /**
     * Delete a category and all its associated products.
     *
     * @param id the category ID to delete
     */
    void deleteCategory(Long id);
}
