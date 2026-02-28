package com.amalitech.smartshop.mappers;

import com.amalitech.smartshop.dtos.responses.OrderItemResponseDTO;
import com.amalitech.smartshop.dtos.responses.OrderResponseDTO;
import com.amalitech.smartshop.entities.Order;
import com.amalitech.smartshop.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Order entity conversions.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "userName", expression = "java(order.getUser() != null ? order.getUser().getFullName() : null)")
    @Mapping(source = "items", target = "items")
    OrderResponseDTO toResponseDTO(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemResponseDTO toOrderItemResponseDTO(OrderItem orderItem);
}
