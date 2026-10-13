package com.dentflow.dto;

import jakarta.validation.constraints.Size;

public record AppointmentCancelRequest(
        @Size(max = 255, message = "Le motif d'annulation est trop long")
        String cancellationReason
) {
}
