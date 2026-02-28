package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.AddInventoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateInventoryDTO;
import com.amalitech.smartshop.dtos.responses.InventoryResponseDTO;
import com.amalitech.smartshop.entities.Inventory;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.exceptions.ConstraintViolationException;
import com.amalitech.smartshop.exceptions.InsufficientStockException;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.InventoryService;
import com.amalitech.smartshop.mappers.InventoryMapper;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryJpaRepository inventoryRepository;
    private final ProductJpaRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public InventoryResponseDTO addInventory(AddInventoryDTO addInventoryDTO) {
        log.info("Adding inventory for product: {}", addInventoryDTO.getProductId());

        Product product = productRepository.findById(addInventoryDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + addInventoryDTO.getProductId()));

        if (inventoryRepository.existsByProduct_Id(addInventoryDTO.getProductId())) {
            throw new ResourceAlreadyExistsException("Inventory already exists for product ID: " + addInventoryDTO.getProductId());
        }

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(addInventoryDTO.getQuantity())
                .location(addInventoryDTO.getLocation())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("Inventory added successfully with id: {}", savedInventory.getId());
        return inventoryMapper.toResponseDTO(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponseDTO> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(inventoryMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
        return inventoryMapper.toResponseDTO(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryByProductId(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));
        return inventoryMapper.toResponseDTO(inventory);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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

        Inventory updatedInventory = inventoryRepository.save(existingInventory);
        return inventoryMapper.toResponseDTO(updatedInventory);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public InventoryResponseDTO adjustInventoryQuantity(Long id, Integer quantityChange) {
        log.info("Adjusting inventory quantity: {} by {}", id, quantityChange);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        int newQuantity = inventory.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new InsufficientStockException(
                    "Insufficient inventory. Available: " + inventory.getQuantity()
                            + ", Required: " + Math.abs(quantityChange));
        }

        inventory.setQuantity(newQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toResponseDTO(updatedInventory);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteInventory(Long id) {
        log.info("Deleting inventory: {}", id);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));

        try {
            inventoryRepository.delete(inventory);
            log.info("Inventory deleted successfully: {}", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("foreign key constraint")) {
                throw new ConstraintViolationException(
                        "Cannot delete inventory. It has related dependencies that must be removed first.");
            }
            throw ex;
        }
    }
}
