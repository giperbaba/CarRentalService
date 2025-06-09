package com.userapp.mapper;

import com.userapp.dto.request.UserRegisterRequestDto;
import com.userapp.dto.request.UserUpdateRequestDto;
import com.userapp.dto.response.UserResponseDto;
import com.userapp.entity.Role;
import com.userapp.entity.User;
import com.userapp.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {
    private static final Logger logger = LoggerFactory.getLogger(UserMapper.class);

    public User registerRequestToUser(UserRegisterRequestDto registerRequest) {
        logger.debug("Mapping UserRegisterRequestDto to User. Email: {}, Birthday: {}", 
                    registerRequest.email(), registerRequest.birthday());

        User user = new User();
        user.setName(registerRequest.name());
        user.setSurname(registerRequest.surname());
        user.setBirthday(registerRequest.birthday());
        user.setEmail(registerRequest.email());
        user.setPassword(registerRequest.password());
        user.setGender(registerRequest.gender());
        user.setPhone(registerRequest.phone());
        user.setActive(true);
        user.setRoles(getDefaultRoles());

        logger.debug("Successfully mapped User entity. Email: {}, Birthday: {}", 
                    user.getEmail(), user.getBirthday());
        return user;
    }

    public UserResponseDto userToUserResponseDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getPhone(),
            user.getName(),
            user.getSurname(),
            user.isActive()
        );
    }

    public void updateUserFromDto(UserUpdateRequestDto dto, User user) {
        if (dto.name() != null) {
            user.setName(dto.name());
        }
        if (dto.surname() != null) {
            user.setSurname(dto.surname());
        }
        if (dto.phone() != null) {
            user.setPhone(dto.phone());
        }
    }

    private Set<Role> getDefaultRoles() {
        Role userRole = new Role();
        userRole.setRole(UserRole.ROLE_USER);
        return Set.of(userRole);
    }
}