package com.amalitech.smartshop;

import com.amalitech.smartshop.entities.Category;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.repositories.jpa.CategoryJpaRepository;
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

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedCategories();
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
}
