package com.analytics.domain.repository;

import com.analytics.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(Role.RoleType name);

    /**
     * Check if role exists by name
     */
    Boolean existsByName(Role.RoleType name);
}
