package com.userapp.mapper;

import com.userapp.dto.request.RegisterRequestDto;
import com.userapp.entity.Role;
import com.userapp.entity.User;
import com.userapp.enums.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", expression = "java(getDefaultRoles())")
    @Mapping(target = "active", constant = "true")
    User registerRequestToUser(RegisterRequestDto registerRequest);

    default Set<Role> getDefaultRoles() {
        Role userRole = new Role();
        userRole.setRole(UserRole.ROLE_USER);
        return Set.of(userRole);
    }
}
