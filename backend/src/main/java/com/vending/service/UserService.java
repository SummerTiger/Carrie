package com.vending.service;

import com.vending.dto.CreateUserRequest;
import com.vending.dto.UpdateUserRequest;
import com.vending.dto.UserDto;
import com.vending.entity.User;
import com.vending.entity.UserRole;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Get all users without pagination
     * @return List of all users as UserDto
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with pagination
     * @param pageable Pagination information
     * @return Page of users as UserDto
     */
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toDto);
    }

    /**
     * Get user by ID
     * @param id User ID
     * @return UserDto
     * @throws ResourceNotFoundException if user not found
     */
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toDto(user);
    }

    /**
     * Create a new user
     * @param request User creation request
     * @return Created user as UserDto
     * @throws DuplicateResourceException if username or email already exists
     */
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        // Build user entity
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .roles(request.roles() != null && !request.roles().isEmpty()
                    ? request.roles()
                    : Set.of(UserRole.VIEWER))
                .enabled(request.enabled() != null ? request.enabled() : true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .failedLoginAttempts(0)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Audit log
        auditLogService.log(
            "CREATE_USER",
            "USER",
            savedUser.getId().toString(),
            String.format("Created user: %s with roles: %s",
                savedUser.getUsername(),
                savedUser.getRoles())
        );

        return toDto(savedUser);
    }

    /**
     * Update an existing user
     * @param id User ID
     * @param request User update request
     * @return Updated user as UserDto
     * @throws ResourceNotFoundException if user not found
     * @throws DuplicateResourceException if email already exists
     */
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check for duplicate email (excluding current user)
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("User", "email", request.email());
            }
            user.setEmail(request.email());
        }

        // Update fields
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.roles() != null && !request.roles().isEmpty()) {
            user.setRoles(request.roles());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.accountNonLocked() != null) {
            user.setAccountNonLocked(request.accountNonLocked());
            // Reset failed login attempts when unlocking account
            if (request.accountNonLocked()) {
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
            }
        }

        User updatedUser = userRepository.save(user);

        // Audit log
        auditLogService.log(
            "UPDATE_USER",
            "USER",
            updatedUser.getId().toString(),
            String.format("Updated user: %s", updatedUser.getUsername())
        );

        return toDto(updatedUser);
    }

    /**
     * Delete a user
     * @param id User ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String username = user.getUsername();
        userRepository.deleteById(id);

        // Audit log
        auditLogService.log(
            "DELETE_USER",
            "USER",
            id.toString(),
            String.format("Deleted user: %s", username)
        );
    }

    /**
     * Get users by role
     * @param role User role
     * @return List of users with the specified role
     */
    public List<UserDto> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert User entity to UserDto
     * @param user User entity
     * @return UserDto
     */
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
