package com.dentflow.service;

import com.dentflow.dto.AuthResponse;
import com.dentflow.dto.PasswordReset;
import com.dentflow.dto.RegisterRequest;
import com.dentflow.model.Role;
import com.dentflow.model.User;
import com.dentflow.repository.UserRepository;
import com.dentflow.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void registrationHashesPasswordAndForcesUserRole() {
        UserRepository repository = mock(UserRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-token");
        when(jwtService.expirationSeconds()).thenReturn(1800L);

        AuthService authService = new AuthService(
                repository,
                passwordEncoder,
                authenticationManager,
                jwtService
        );

        RegisterRequest request = new RegisterRequest(
                "alice",
                "Alice",
                "Martin",
                "Alice@Example.com",
                "password123"
        );

        AuthResponse response = authService.register(request);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("signed-token", response.accessToken());
        assertEquals(Role.USER, response.user().role());
        assertEquals("alice@example.com", response.user().email());
        assertNotEquals("password123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertTrue(response.message().contains("Alice"));
    }

    @Test
    void passwordResetVerifiesCurrentPasswordAndStoresNewHash() {
        UserRepository repository = mock(UserRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setFirstName("Alice");
        user.setLastName("Martin");
        user.setEmail("alice@example.com");
        user.setPassword(passwordEncoder.encode("currentPassword"));
        user.setRole(Role.USER);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(user)).thenReturn("updated-token");
        when(jwtService.expirationSeconds()).thenReturn(1800L);

        AuthService authService = new AuthService(
                repository,
                passwordEncoder,
                authenticationManager,
                jwtService
        );

        AuthResponse response = authService.resetPassword("1", new PasswordReset(
                " Alice@Example.com ",
                "currentPassword",
                "newPassword123",
                "newPassword123"
        ));

        assertEquals("updated-token", response.accessToken());
        assertTrue(passwordEncoder.matches("newPassword123", user.getPassword()));
        verify(repository).save(user);
    }

    @Test
    void passwordResetRejectsIncorrectCurrentPassword() {
        UserRepository repository = mock(UserRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword(passwordEncoder.encode("currentPassword"));
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        AuthService authService = new AuthService(
                repository,
                passwordEncoder,
                authenticationManager,
                jwtService
        );

        assertThrows(BadCredentialsException.class, () -> authService.resetPassword("1", new PasswordReset(
                "alice@example.com",
                "wrongPassword",
                "newPassword123",
                "newPassword123"
        )));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void passwordResetCannotTargetAnotherUsersEmail() {
        UserRepository repository = mock(UserRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        AuthService authService = new AuthService(
                repository,
                passwordEncoder,
                authenticationManager,
                jwtService
        );

        assertThrows(EntityNotFoundException.class, () -> authService.resetPassword("1", new PasswordReset(
                "bob@example.com",
                "currentPassword",
                "newPassword123",
                "newPassword123"
        )));
        verify(repository, never()).save(any(User.class));
    }
}
