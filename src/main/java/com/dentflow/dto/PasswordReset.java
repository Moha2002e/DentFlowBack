package com.dentflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordReset(
        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail n'est pas valide")
        String email,
        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String password,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le nouveau mot de passe doit contenir entre 8 et 72 caracteres")
        String newPassWord,

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        String confirmPassword
) {
}
