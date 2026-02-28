package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddInventoryDTO;
import com.amalitech.smartshop.dtos.responses.InventoryResponseDTO;
import com.amalitech.smartshop.dtos.requests.UpdateInventoryDTO;
import com.amalitech.smartshop.entities.Inventory;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.mappers.InventoryMapper;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    private InventoryServiceImpl inventoryService;

    @Mock
    private InventoryJpaRepository inventoryRepository;

    @Mock
    private ProductJpaRepository productRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryService = new InventoryServiceImpl(inventoryRepository, productRepository, inventoryMapper);
    }

    @Test
    void addInventory_Success() {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(1L);
        dto.setQuantity(100);
        dto.setLocation("Warehouse A");

        Product product = new Product();
        product.setId(1L);

        Inventory savedEntity = new Inventory();
        savedEntity.setId(1L);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.existsByProduct_Id(1L)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedEntity);
        when(inventoryMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.addInventory(dto);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void addInventory_ProductNotFound() {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.addInventory(dto));
    }

    @Test
    void addInventory_AlreadyExists() {
        AddInventoryDTO dto = new AddInventoryDTO();
        dto.setProductId(1L);

        Product product = new Product();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.existsByProduct_Id(1L)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> inventoryService.addInventory(dto));
    }

    @Test
    void getInventoryById_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        Inventory entity = new Inventory();
        entity.setId(1L);
        entity.setProduct(product);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(inventoryMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.getInventoryById(1L);

        assertNotNull(result);
    }

    @Test
    void getInventoryById_NotFound() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventoryById(1L));
    }

    @Test
    void getInventoryByProductId_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        Inventory entity = new Inventory();
        entity.setId(1L);
        entity.setProduct(product);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct_Id(1L)).thenReturn(Optional.of(entity));
        when(inventoryMapper.toResponseDTO(entity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.getInventoryByProductId(1L);

        assertNotNull(result);
    }

    @Test
    void updateInventory_Success() {
        UpdateInventoryDTO updateDTO = new UpdateInventoryDTO();
        updateDTO.setQuantity(200);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        Inventory existingEntity = new Inventory();
        existingEntity.setId(1L);
        existingEntity.setProduct(product);

        Inventory updatedEntity = new Inventory();
        updatedEntity.setId(1L);
        updatedEntity.setProduct(product);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(inventoryRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(inventoryMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.updateInventory(1L, updateDTO);

        assertNotNull(result);
    }

    @Test
    void deleteInventory_Success() {
        Inventory entity = new Inventory();
        entity.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> inventoryService.deleteInventory(1L));
        verify(inventoryRepository).delete(entity);
    }

    @Test
    void adjustInventoryQuantity_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        Inventory entity = new Inventory();
        entity.setId(1L);
        entity.setProduct(product);
        entity.setQuantity(100);

        Inventory updatedEntity = new Inventory();
        updatedEntity.setId(1L);
        updatedEntity.setQuantity(150);

        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setId(1L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(inventoryRepository.save(entity)).thenReturn(updatedEntity);
        when(inventoryMapper.toResponseDTO(updatedEntity)).thenReturn(responseDTO);

        InventoryResponseDTO result = inventoryService.adjustInventoryQuantity(1L, 50);

        assertNotNull(result);
    }
}
