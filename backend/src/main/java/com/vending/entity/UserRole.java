package com.vending.entity;

public enum UserRole {
    ADMIN,      // Full system access including user management
    MANAGER,    // Can manage products, machines, procurement, and files
    OPERATOR,   // Can restock machines and manage inventory
    VIEWER      // Read-only access
}
