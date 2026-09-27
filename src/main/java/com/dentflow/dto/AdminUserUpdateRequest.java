package com.dentflow.dto;

import com.dentflow.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire") String username,
        @NotBlank(message = "Le prenom est obligatoire") String firstName,
        @NotBlank(message = "Le nom est obligatoire") String lastName,
        @NotBlank(message = "L'adresse e-mail est obligatoire") @Email(message = "L'adresse e-mail n'est pas valide") String email,
        @NotNull(message = "Le role est obligatoire") Role role
) {
}
