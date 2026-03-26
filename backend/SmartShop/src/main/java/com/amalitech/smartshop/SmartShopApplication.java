package com.amalitech.smartshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the SmartShop E-Commerce Application.
 * This application provides a comprehensive e-commerce backend service
 * with REST and GraphQL APIs for managing users, products, categories,
 * orders, and inventory.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SmartShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartShopApplication.class, args);
    }
}
