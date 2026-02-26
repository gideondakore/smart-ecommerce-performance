package com.amalitech.smartshop.services;

import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.dtos.requests.AddInventoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateInventoryDTO;
import com.amalitech.smartshop.dtos.responses.InventoryResponseDTO;
import com.amalitech.smartshop.entities.Inventory;
import com.amalitech.smartshop.exceptions.ConstraintViolationException;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.InventoryService;
import com.amalitech.smartshop.mappers.InventoryMapper;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation of the InventoryService interface.
 * Handles all inventory-related business logic including CRUD operations
 * and quantity adjustments.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryJpaRepository inventoryRepository;
    private final ProductJpaRepository productRepository;
    private final InventoryMapper inventoryMapper;
    private final CacheManager cacheManager;

    @Override
    public InventoryResponseDTO addInventory(AddInventoryDTO addInventoryDTO) {
        log.info("Adding inventory for product: {}", addInventoryDTO.getProductId());
        
        productRepository.findById(addInventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + addInventoryDTO.getProductId()));

        if (inventoryRepository.existsByProductId(addInventoryDTO.getProductId())) {
            throw new ResourceAlreadyExistsException("Inventory already exists for product ID: " + addInventoryDTO.getProductId());
        }

        Inventory inventory = Inventory.builder()
                .productId(addInventoryDTO.getProductId())
                .quantity(addInventoryDTO.getQuantity())
                .location(addInventoryDTO.getLocation())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        InventoryResponseDTO response = inventoryMapper.toResponseDTO(savedInventory);
        enrichResponseWithProductName(response, savedInventory.getProductId());
        
        log.info("Inventory added successfully with id: {}", savedInventory.getId());
        cacheManager.invalidate("invent:" + addInventoryDTO.getProductId());
        return response;
    }

    @Override
    public Page<InventoryResponseDTO> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable)
                .map(inventory -> cacheManager.get("inventory:" + inventory.getId(), () -> {
                    InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);
                    enrichResponseWithProductName(response, inventory.getProductId());
                    return response;
                }));
    }

    @Override
    public InventoryResponseDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);
        enrichResponseWithProductName(response, inventory.getProductId());
        return response;
    }

    @Override
    public InventoryResponseDTO getInventoryByProductId(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));
        InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);
        enrichResponseWithProductName(response, inventory.getProductId());
        return response;
    }

    @Override
    public InventoryResponseDTO updateInventory(Long id, UpdateInventoryDTO updateInventoryDTO) {
        log.info("Updating inventory: {}", id);
        
        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        if (updateInventoryDTO.getQuantity() != null) {
            existingInventory.setQuantity(updateInventoryDTO.getQuantity());
        }

        if (updateInventoryDTO.getLocation() != null) {
            existingInventory.setLocation(updateInventoryDTO.getLocation());
        }

        return saveAndBuildResponse(id, existingInventory);
    }

    @Override
    public InventoryResponseDTO adjustInventoryQuantity(Long id, Integer quantityChange) {
        log.info("Adjusting inventory quantity: {} by {}", id, quantityChange);
        
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        int newQuantity = inventory.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient inventory. Available: " + inventory.getQuantity() + ", Required: " + Math.abs(quantityChange));
        }

        inventory.setQuantity(newQuantity);
        return saveAndBuildResponse(id, inventory);
    }

    @Override
    public void deleteInventory(Long id) {
        log.info("Deleting inventory: {}", id);
        
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        Long productId = inventory.getProductId();

        try {
            inventoryRepository.delete(inventory);
            invalidateInventoryCache(id, productId);
            log.info("Inventory deleted successfully: {}", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
                throw new ConstraintViolationException(
                        "Cannot delete inventory. It has related dependencies that must be removed first.");
            }
            throw ex;
        }
    }

    private InventoryResponseDTO saveAndBuildResponse(Long id, Inventory inventory) {
        Inventory updatedInventory = inventoryRepository.save(inventory);
        invalidateInventoryCache(id, inventory.getProductId());

        InventoryResponseDTO response = inventoryMapper.toResponseDTO(updatedInventory);
        enrichResponseWithProductName(response, updatedInventory.getProductId());
        return response;
    }

    private void enrichResponseWithProductName(InventoryResponseDTO response, Long productId) {
        productRepository.findById(productId)
                .ifPresent(product -> response.setProductName(product.getName()));
    }

    private void invalidateInventoryCache(Long inventoryId, Long productId) {
        cacheManager.invalidate("invent:" + inventoryId);
        cacheManager.invalidate("invent:" + productId);
        cacheManager.invalidate("invent:" + productId);
        cacheManager.invalidate("prod:" + productId);
    }
}
