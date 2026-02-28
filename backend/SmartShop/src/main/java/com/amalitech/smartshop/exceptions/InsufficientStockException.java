package com.amalitech.smartshop.exceptions;

/**
 * Thrown when an order cannot be fulfilled because a product's
 * inventory quantity is less than the requested amount.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
