package com.dentflow.dto;

/** Compteurs du planning, calcules en base : le front ne fait que les afficher. */
public record AppointmentStatsResponse(
        long total,
        long upcoming,
        long completed,
        long cancelled
) {
}
