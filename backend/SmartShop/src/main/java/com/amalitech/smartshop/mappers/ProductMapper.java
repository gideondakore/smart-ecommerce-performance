package com.amalitech.smartshop.mappers;

import com.amalitech.smartshop.dtos.requests.AddProductDTO;
import com.amalitech.smartshop.dtos.requests.UpdateProductDTO;
import com.amalitech.smartshop.dtos.responses.ProductResponseDTO;
import com.amalitech.smartshop.entities.Product;
import org.mapstruct.*;

/**
 * MapStruct mapper for Product entity conversions.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "inventory.quantity", target = "quantity")
    ProductResponseDTO toResponseDTO(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(AddProductDTO addProductDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "available", source = "isAvailable")
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductDTO updateDTO, @MappingTarget Product entity);
}
