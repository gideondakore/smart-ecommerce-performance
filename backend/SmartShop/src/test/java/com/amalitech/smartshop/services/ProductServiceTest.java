package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.mappers.ProductMapper;
import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductServiceImpl productService;

    @Mock
    private ProductJpaRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryJpaRepository categoryRepository;

    @Mock
    private InventoryJpaRepository inventoryRepository;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductServiceImpl(productRepository, productMapper, categoryRepository, inventoryRepository, cacheManager);
    }

    @Test
    void addProduct_Success() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Laptop");
        dto.setCategoryId(1L);
        dto.setSku("LAP001");
        dto.setPrice(999.99);
        
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        
        Product entity = new Product();
        Product savedEntity = new Product();
        savedEntity.setId(1L);
        savedEntity.setCategoryId(1L);
        
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(dto)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(savedEntity);
        when(productMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        ProductResponseDTO result = productService.addProduct(dto, 1L, "VENDOR");

        assertNotNull(result);
        verify(productRepository).save(entity);
    }

    @Test
    void addProduct_AlreadyExists() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Laptop");

        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> productService.addProduct(dto, 1L, "VENDOR"));
    }

    @Test
    void addProduct_CategoryNotFound() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Laptop");
        dto.setCategoryId(1L);

        when(productRepository.existsByNameIgnoreCase("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(dto, 1L, "VENDOR"));
    }

    @Test
    void getProductById_Success() {
        Product entity = new Product();
        entity.setId(1L);
        entity.setCategoryId(1L);
        
        Category category = new Category();
        category.setName("Electronics");
        
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toResponseDTO(entity)).thenReturn(responseDTO);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        ProductResponseDTO result = productService.getProductById(1L);

        assertNotNull(result);
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void updateProduct_Success() {
        UpdateProductDTO updateDTO = new UpdateProductDTO();
        updateDTO.setName("Updated Laptop");
        
        Product existingEntity = new Product();
        existingEntity.setId(1L);
        existingEntity.setName("Laptop");
        existingEntity.setCategoryId(1L);
        
        Product updatedEntity = new Product();
        updatedEntity.setId(1L);
        updatedEntity.setName("Updated Laptop");
        updatedEntity.setCategoryId(1L);
        
        Category category = new Category();
        category.setName("Electronics");
        
        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(productRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        ProductResponseDTO result = productService.updateProduct(1L, updateDTO);

        assertNotNull(result);
        verify(productMapper).updateEntity(updateDTO, existingEntity);
    }

    @Test
    void deleteProduct_Success() {
        Product entity = new Product();
        entity.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).delete(entity);
    }
}
