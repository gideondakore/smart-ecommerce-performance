package com.amalitech.smartshop;

import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.Inventory;
import com.amalitech.smartshop.entities.Product;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.InventoryJpaRepository;
import com.amalitech.smartshop.repositories.jpa.ProductJpaRepository;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Database seeder for initial application data.
 * Creates default users and categories when the application starts
 * if they don't already exist.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SeedData implements CommandLineRunner {

    private final UserJpaRepository userRepository;
    private final CategoryJpaRepository categoryRepository;
    private final ProductJpaRepository productRepository;
    private final InventoryJpaRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedCategories();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() <= 10) {
            log.info("Seeding users...");

            // Admin user
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@smartshop.com");
            admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);

            // Vendor user
            User vendor = new User();
            vendor.setFirstName("John");
            vendor.setLastName("Vendor");
            vendor.setEmail("vendor@smartshop.com");
            vendor.setPassword(BCrypt.hashpw("vendor123", BCrypt.gensalt()));
            vendor.setRole(UserRole.VENDOR);
            userRepository.save(vendor);

            // Customer users
            User customer1 = new User();
            customer1.setFirstName("Jane");
            customer1.setLastName("Doe");
            customer1.setEmail("jane.doe@example.com");
            customer1.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer1.setRole(UserRole.CUSTOMER);
            userRepository.save(customer1);

            User customer2 = new User();
            customer2.setFirstName("Mike");
            customer2.setLastName("Smith");
            customer2.setEmail("mike.smith@example.com");
            customer2.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer2.setRole(UserRole.CUSTOMER);
            userRepository.save(customer2);

            User customer3 = new User();
            customer3.setFirstName("Sarah");
            customer3.setLastName("Johnson");
            customer3.setEmail("sarah.johnson@example.com");
            customer3.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer3.setRole(UserRole.CUSTOMER);
            userRepository.save(customer3);

            User customer4 = new User();
            customer4.setFirstName("David");
            customer4.setLastName("Brown");
            customer4.setEmail("david.brown@example.com");
            customer4.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer4.setRole(UserRole.CUSTOMER);
            userRepository.save(customer4);

            User customer5 = new User();
            customer5.setFirstName("Emily");
            customer5.setLastName("Davis");
            customer5.setEmail("emily.davis@example.com");
            customer5.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer5.setRole(UserRole.CUSTOMER);
            userRepository.save(customer5);

            User vendor2 = new User();
            vendor2.setFirstName("Robert");
            vendor2.setLastName("Wilson");
            vendor2.setEmail("robert.wilson@smartshop.com");
            vendor2.setPassword(BCrypt.hashpw("vendor123", BCrypt.gensalt()));
            vendor2.setRole(UserRole.VENDOR);
            userRepository.save(vendor2);

            User customer6 = new User();
            customer6.setFirstName("Lisa");
            customer6.setLastName("Martinez");
            customer6.setEmail("lisa.martinez@example.com");
            customer6.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer6.setRole(UserRole.CUSTOMER);
            userRepository.save(customer6);

            User customer7 = new User();
            customer7.setFirstName("James");
            customer7.setLastName("Taylor");
            customer7.setEmail("james.taylor@example.com");
            customer7.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer7.setRole(UserRole.CUSTOMER);
            userRepository.save(customer7);

            User customer8 = new User();
            customer8.setFirstName("Maria");
            customer8.setLastName("Garcia");
            customer8.setEmail("maria.garcia@example.com");
            customer8.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer8.setRole(UserRole.CUSTOMER);
            userRepository.save(customer8);

            User customer9 = new User();
            customer9.setFirstName("Chris");
            customer9.setLastName("Anderson");
            customer9.setEmail("chris.anderson@example.com");
            customer9.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer9.setRole(UserRole.CUSTOMER);
            userRepository.save(customer9);

            User customer10 = new User();
            customer10.setFirstName("Amanda");
            customer10.setLastName("Thomas");
            customer10.setEmail("amanda.thomas@example.com");
            customer10.setPassword(BCrypt.hashpw("customer123", BCrypt.gensalt()));
            customer10.setRole(UserRole.CUSTOMER);
            userRepository.save(customer10);

            log.info("Seeded {} users successfully", userRepository.count());
        } else {
            log.info("Users already exist. Skipping seed data.");
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Seeding categories...");

            Category electronics = new Category();
            electronics.setName("Electronics");
            electronics.setDescription("Devices and gadgets including phones, laptops, and accessories.");
            categoryRepository.save(electronics);

            Category fashion = new Category();
            fashion.setName("Fashion");
            fashion.setDescription("Clothing, shoes, and accessories for men and women.");
            categoryRepository.save(fashion);

            Category home = new Category();
            home.setName("Home & Garden");
            home.setDescription("Home improvement, furniture, and garden supplies.");
            categoryRepository.save(home);

            Category sports = new Category();
            sports.setName("Sports & Outdoors");
            sports.setDescription("Sports equipment, fitness gear, and outdoor recreation.");
            categoryRepository.save(sports);

            log.info("Seeded {} categories successfully", categoryRepository.count());
        } else {
            log.info("Categories already exist. Skipping seed data.");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            log.info("Seeding products...");

            Long vendorId = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.VENDOR)
                    .findFirst()
                    .map(User::getId)
                    .orElse(null);

            Product laptop = Product.builder()
                    .name("Laptop Pro 15")
                    .description("High-performance laptop with 16GB RAM")
                    .categoryId(1L)
                    .sku("LAP-001")
                    .price(1299.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(laptop);
            inventoryRepository.save(Inventory.builder().productId(laptop.getId()).quantity(50).location("Warehouse A").build());

            Product mouse = Product.builder()
                    .name("Wireless Mouse")
                    .description("Ergonomic wireless mouse")
                    .categoryId(1L)
                    .sku("MOU-001")
                    .price(29.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(mouse);
            inventoryRepository.save(Inventory.builder().productId(mouse.getId()).quantity(100).location("Warehouse A").build());

            Product cable = Product.builder()
                    .name("USB-C Cable")
                    .description("Fast charging USB-C cable")
                    .categoryId(1L)
                    .sku("CAB-001")
                    .price(12.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(cable);
            inventoryRepository.save(Inventory.builder().productId(cable.getId()).quantity(200).location("Warehouse B").build());

            Product tshirt = Product.builder()
                    .name("T-Shirt Blue")
                    .description("Cotton blue t-shirt")
                    .categoryId(2L)
                    .sku("TSH-001")
                    .price(19.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(tshirt);
            inventoryRepository.save(Inventory.builder().productId(tshirt.getId()).quantity(75).location("Warehouse C").build());

            Product jeans = Product.builder()
                    .name("Jeans Classic")
                    .description("Classic fit jeans")
                    .categoryId(2L)
                    .sku("JEA-001")
                    .price(49.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(jeans);
            inventoryRepository.save(Inventory.builder().productId(jeans.getId()).quantity(60).location("Warehouse C").build());

            Product hose = Product.builder()
                    .name("Garden Hose")
                    .description("50ft expandable garden hose")
                    .categoryId(3L)
                    .sku("HOS-001")
                    .price(34.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(hose);
            inventoryRepository.save(Inventory.builder().productId(hose.getId()).quantity(40).location("Warehouse B").build());

            Product basketball = Product.builder()
                    .name("Basketball")
                    .description("Official size basketball")
                    .categoryId(4L)
                    .sku("BAS-001")
                    .price(24.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(basketball);
            inventoryRepository.save(Inventory.builder().productId(basketball.getId()).quantity(80).location("Warehouse A").build());

            Product yogaMat = Product.builder()
                    .name("Yoga Mat")
                    .description("Non-slip yoga mat")
                    .categoryId(4L)
                    .sku("YOG-001")
                    .price(29.99)
                    .imageUrl("https://placehold.net/1.png")
                    .available(true)
                    .vendorId(vendorId)
                    .build();
            productRepository.save(yogaMat);
            inventoryRepository.save(Inventory.builder().productId(yogaMat.getId()).quantity(90).location("Warehouse C").build());

            log.info("Seeded {} products successfully", productRepository.count());
        } else {
            log.info("Products already exist. Skipping seed data.");
        }
    }
}
