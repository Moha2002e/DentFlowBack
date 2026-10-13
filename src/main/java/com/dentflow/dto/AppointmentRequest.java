package com.dentflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "Le patient est obligatoire")
        Long patientId,

        @NotNull(message = "Le dentiste est obligatoire")
        Long dentistId,

        Long treatmentId,

        @NotNull(message = "La date du rendez-vous est obligatoire")
        LocalDateTime appointmentDate,

        @Min(value = 5, message = "La duree doit etre d'au moins 5 minutes")
        @Max(value = 480, message = "La duree ne peut pas depasser 8 heures")
        Integer durationMinutes,

        @Size(max = 255, message = "Le motif est trop long")
        String reason,

        @Size(max = 2000, message = "Les notes sont trop longues")
        String notes
) {
}
