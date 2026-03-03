package com.amalitech.smartshop.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SmartShop API",
                version = "1.0.0",
                description = """
                        # SmartShop E-Commerce Platform API
                        
                        Welcome to SmartShop's RESTful API - your gateway to building modern shopping experiences. 
                        Our platform delivers complete e-commerce capabilities:
                        - Secure user identity and account creation
                        - Comprehensive product browsing and search
                        - Seamless cart management and order fulfillment
                        - Real-time order status monitoring
                        - Dynamic inventory control
                        - Flexible category organization
                        
                        ## Security & Access Control
                        Protected endpoints require a valid Bearer token included in your request headers.
                        Header format: `Authorization: Bearer <your-token-here>`
                        
                        ## Response Codes & Error Patterns
                        SmartShop API follows REST conventions with standard HTTP status codes for all responses:
                        
                        ### Status Code Reference:
                        - **400 Bad Request**: Invalid input data or failed validation checks
                        - **401 Unauthorized**: Authentication credentials missing or expired
                        - **403 Forbidden**: Insufficient privileges for the requested operation
                        - **404 Not Found**: Requested resource doesn't exist in our system
                        - **409 Conflict**: Duplicate resource or business rule violation
                        - **500 Internal Server Error**: An unexpected issue occurred on our end
                        
                        ### Error Payload Examples:
                        
                        **General Error Format** (401, 403, 404, 500):
                        ```json
                        {
                          "timestamp": "2026-01-19T10:30:00.000+00:00",
                          "status": 404,
                          "message": "Product not found with id: 123",
                          "path": "uri=/api/products/123"
                        }
                        ```
                        
                        **Validation Failure Format** (400):
                        ```json
                        {
                          "timestamp": "2026-01-19T10:30:00.000+00:00",
                          "status": 400,
                          "errors": {
                            "email": "must be a well-formed email address",
                            "name": "must not be blank",
                            "price": "must be greater than 0"
                          },
                          "path": "uri=/api/products/add"
                        }
                        ```
                        
                        ## User Roles & Access Levels
                        - **CUSTOMER**: Browse catalog, place orders, manage personal shopping cart and order history
                        - **ADMIN**: Complete system control with access to all resources and administrative functions
                        """,
                contact = @Contact(
                        name = "SmartShop API Support",
                        email = "support@smartshop.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
                @Server(url = "https://api.smartshop.com", description = "Production Server")
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();

        components.addSecuritySchemes("BearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token"));

        components.addResponses("BadRequest", new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Invalid Request - Input validation failed or request body is improperly formatted")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ValidationErrorResponse")))));

        components.addResponses("Unauthorized", new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Access Denied - Authentication credentials are absent or have expired")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));

        components.addResponses("Forbidden", new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Insufficient Privileges - Your account lacks the necessary permissions for this action")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));

        components.addResponses("NotFound", new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Resource Unavailable - The requested item could not be located in our system")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));

        components.addResponses("Conflict", new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Operation Conflict - Resource duplication detected or business constraint violated")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));

        components.addResponses("InternalServerError", new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Server Malfunction - An unforeseen error has occurred while processing your request")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));

        return new OpenAPI().components(components);
    }
}
