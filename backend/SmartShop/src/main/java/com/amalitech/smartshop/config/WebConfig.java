package com.amalitech.smartshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * CORS is now handled by Spring Security's CorsConfigurationSource bean.
 * Authentication and authorization are handled by Spring Security filters.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
