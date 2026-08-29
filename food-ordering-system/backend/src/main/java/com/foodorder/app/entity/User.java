package com.foodorder.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodorder.app.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents both Customers and Restaurant Admins.
 * The `role` field distinguishes which module of the app they can access.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Stored as a BCrypt hash - never returned to the client
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
