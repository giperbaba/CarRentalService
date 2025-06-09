package com.userapp.mapper;

import com.userapp.dto.request.UserRegisterRequestDto;
import com.userapp.dto.request.UserUpdateRequestDto;
import com.userapp.dto.response.UserResponseDto;
import com.userapp.entity.Role;
import com.userapp.entity.User;
import com.userapp.enums.UserRole;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")

public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", expression = "java(getDefaultRoles())")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "authorities", ignore = true)
    User registerRequestToUser(UserRegisterRequestDto registerRequest);

    UserResponseDto userToUserResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateUserFromDto(UserUpdateRequestDto dto, @MappingTarget User user);


    default Set<Role> getDefaultRoles() {
        Role userRole = new Role();
        userRole.setRole(UserRole.ROLE_USER);
        return Set.of(userRole);
    }

    default Set<UserRole> mapRolesToStrings(Set<Role> roles) {
        return roles.stream()
                .map(Role::getRole)
                .collect(Collectors.toSet());
    }
}