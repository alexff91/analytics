package com.analytics.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Role Entity
 * Represents user roles for authorization
 */
@Entity
@Table(name = "roles",
       uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private RoleType name;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * Role types
     */
    public enum RoleType {
        ROLE_USER,
        ROLE_MANAGER,
        ROLE_ADMIN
    }
}
