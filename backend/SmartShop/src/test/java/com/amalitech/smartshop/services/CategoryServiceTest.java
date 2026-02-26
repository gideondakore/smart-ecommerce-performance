package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddCategoryDTO;
import com.amalitech.smartshop.dtos.responses.CategoryResponseDTO;
import com.amalitech.smartshop.dtos.requests.UpdateCategoryDTO;
import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.mappers.CategoryMapper;
import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryJpaRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ProductJpaRepository productRepository;

    @Mock
    private InventoryJpaRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryServiceImpl(categoryRepository, categoryMapper, cacheManager, productRepository, inventoryRepository);
    }

    @Test
    void addCategory_Success() {
        AddCategoryDTO dto = new AddCategoryDTO();
        dto.setName("Electronics");
        dto.setDescription("Electronic items");
        
        Category entity = new Category();
        Category savedEntity = new Category();
        savedEntity.setId(1L);
        
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(false);
        when(categoryMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(savedEntity);
        when(categoryMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.addCategory(dto);

        assertNotNull(result);
        verify(categoryRepository).save(entity);
    }

    @Test
    void addCategory_AlreadyExists() {
        AddCategoryDTO dto = new AddCategoryDTO();
        dto.setName("Electronics");

        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.addCategory(dto));
    }

    @Test
    void getCategoryById_Success() {
        Category entity = new Category();
        entity.setId(1L);
        
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.getCategoryById(1L);

        assertNotNull(result);
    }

    @Test
    void getCategoryById_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void updateCategory_Success() {
        UpdateCategoryDTO updateDTO = new UpdateCategoryDTO();
        updateDTO.setName("Updated Electronics");
        
        Category existingEntity = new Category();
        existingEntity.setId(1L);
        existingEntity.setName("Electronics");
        
        Category updatedEntity = new Category();
        updatedEntity.setId(1L);
        updatedEntity.setName("Updated Electronics");
        
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(categoryRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(categoryMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);

        CategoryResponseDTO result = categoryService.updateCategory(1L, updateDTO);

        assertNotNull(result);
        verify(categoryMapper).updateEntity(updateDTO, existingEntity);
    }

    @Test
    void deleteCategory_Success() {
        Category entity = new Category();
        entity.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productRepository.findByCategoryId(1L)).thenReturn(java.util.Collections.emptyList());

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository).delete(entity);
    }
}
