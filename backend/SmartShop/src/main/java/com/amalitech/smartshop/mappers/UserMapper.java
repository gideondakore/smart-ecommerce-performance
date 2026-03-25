package com.amalitech.smartshop.mappers;

import com.amalitech.smartshop.dtos.requests.UpdateUserDTO;
import com.amalitech.smartshop.dtos.requests.UserRegistrationDTO;
import com.amalitech.smartshop.dtos.responses.LoginResponseDTO;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.entities.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity conversions.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    LoginResponseDTO toResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2Id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "sessions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    User toEntity(UserRegistrationDTO userRegistrationDTO);

    UserSummaryDTO toSummaryDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2Id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "sessions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserDTO updateDTO, @MappingTarget User entity);
}
