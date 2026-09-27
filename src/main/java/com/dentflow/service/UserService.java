package com.dentflow.service;

import com.dentflow.dto.AdminUserUpdateRequest;
import com.dentflow.dto.AdminUserCreateRequest;
import com.dentflow.dto.UserResponse;
import com.dentflow.model.User;
import com.dentflow.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(AdminUserCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();

        if (userRepository.existsByEmailIgnoreCase(email)
                || userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalStateException("Cette adresse e-mail ou ce nom d'utilisateur est deja utilise");
        }

        User user = new User();
        user.setUsername(username);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        User user = findUser(id);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)
                || userRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new IllegalStateException("Cette adresse e-mail ou ce nom d'utilisateur est deja utilise");
        }

        user.setUsername(username);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setRole(request.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse disableUser(Long id) {
        User user = findUser(id);
        user.setEnabled(false);
        return UserResponse.from(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
    }

}
