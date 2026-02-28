package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.dtos.requests.AddInventoryDTO;
import com.amalitech.smartshop.dtos.requests.UpdateInventoryDTO;
import com.amalitech.smartshop.dtos.responses.InventoryResponseDTO;
import com.amalitech.smartshop.dtos.responses.LowStockDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for inventory-related business operations.
 */
public interface InventoryService {

    /**
     * Add a new inventory record.
     *
     * @param addInventoryDTO the inventory data
     * @return the created inventory response
     */
    InventoryResponseDTO addInventory(AddInventoryDTO addInventoryDTO);

    /**
     * Get all inventory records with pagination.
     *
     * @param pageable pagination information
     * @return a page of inventory responses
     */
    Page<InventoryResponseDTO> getAllInventories(Pageable pageable);

    /**
     * Get an inventory record by its ID.
     *
     * @param id the inventory ID
     * @return the inventory response
     */
    InventoryResponseDTO getInventoryById(Long id);

    /**
     * Get inventory by product ID.
     *
     * @param productId the product ID
     * @return the inventory response
     */
    InventoryResponseDTO getInventoryByProductId(Long productId);

    /**
     * Update an inventory record.
     *
     * @param id the inventory ID
     * @param updateInventoryDTO the update data
     * @return the updated inventory response
     */
    InventoryResponseDTO updateInventory(Long id, UpdateInventoryDTO updateInventoryDTO);

    /**
     * Adjust inventory quantity by a delta value.
     *
     * @param id the inventory ID
     * @param quantityChange the quantity change (positive or negative)
     * @return the updated inventory response
     */
    InventoryResponseDTO adjustInventoryQuantity(Long id, Integer quantityChange);

    /**
     * Delete an inventory record.
     *
     * @param id the inventory ID to delete
     */
    void deleteInventory(Long id);

    /**
     * Set exact quantity for a product's inventory.
     *
     * @param productId the product ID
     * @param quantity the new quantity
     */
    void updateQuantityByProductId(Long productId, Integer quantity);

    /**
     * Atomically decrement stock for a product.
     *
     * @param productId the product ID
     * @param quantity the quantity to decrement
     */
    void decrementStock(Long productId, Integer quantity);

    /**
     * Atomically increment stock for a product.
     *
     * @param productId the product ID
     * @param quantity the quantity to increment
     */
    void incrementStock(Long productId, Integer quantity);

    /**
     * Delete inventory by product ID.
     *
     * @param productId the product ID
     */
    void deleteInventoryByProductId(Long productId);

    /**
     * Find inventory items at or below a stock threshold.
     *
     * @param threshold the maximum quantity
     * @return list of low stock items
     */
    List<LowStockDTO> getLowStockItems(Integer threshold);
}
