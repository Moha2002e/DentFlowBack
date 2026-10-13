package com.dentflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DentistRequest(
        @NotBlank(message = "Le prenom est obligatoire")
        @Size(max = 100, message = "Le prenom est trop long")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom est trop long")
        String lastName,

        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail n'est pas valide")
        String email,

        @Size(max = 30, message = "Le numero de telephone est trop long")
        String phoneNumber,

        @Size(max = 120, message = "La specialisation est trop longue")
        String specialization,

        @Size(max = 20, message = "Le numero INAMI est trop long")
        String inami,

        @Size(max = 255, message = "L'adresse est trop longue")
        String address,

        @Size(max = 100, message = "La ville est trop longue")
        String city,

        @Size(max = 20, message = "Le code postal est trop long")
        String postalCode,

        @Size(max = 34, message = "L'IBAN est trop long")
        String iban
) {
}
