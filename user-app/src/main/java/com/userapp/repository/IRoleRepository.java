package com.userapp.repository;

import com.userapp.entity.Role;
import com.userapp.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IRoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRole(UserRole role);
}
