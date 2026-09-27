package com.dentflow.service;

import com.dentflow.dto.*;
import com.dentflow.model.Role;
import com.dentflow.model.User;
import com.dentflow.repository.UserRepository;
import com.dentflow.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = request.username().trim();

        if (userRepository.existsByEmailIgnoreCase(email) || userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalStateException("Cette adresse e-mail  ou le nom d'utilisateur est deja utilisee");
        }

        User user = new User();
        user.setUsername(username);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return responseFor(savedUser, "Bienvenue " + savedUser.getFirstName() + " ! Votre compte a ete cree.");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

        return responseFor(user, "Bonjour " + user.getFirstName() + ", bienvenue !");
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String subject) {
        try {
            Long userId = Long.valueOf(subject);
            return userRepository.findById(userId)
                    .map(UserResponse::from)
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        } catch (NumberFormatException exception) {
            throw new EntityNotFoundException("Utilisateur introuvable");
        }
    }

    private AuthResponse responseFor(User user, String message) {
        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.expirationSeconds(),
                UserResponse.from(user),
                message
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthResponse resetPassword(String subject, @Valid PasswordReset request) {
        String email = normalizeEmail(request.email());
        User user;
        try {
            Long userId = Long.valueOf(subject);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));
        } catch (NumberFormatException exception) {
            throw new EntityNotFoundException("Utilisateur introuvable");
        }
        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new EntityNotFoundException("Utilisateur introuvable");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Le mot de passe actuel est incorrect");
        }
        if (!request.newPassWord().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe et sa confirmation ne correspondent pas");
        }

        user.setPassword(passwordEncoder.encode(request.newPassWord()));
        userRepository.save(user);
        return responseFor(user, "Le mot de passe a ete reinitialise avec succes");
    }
}
