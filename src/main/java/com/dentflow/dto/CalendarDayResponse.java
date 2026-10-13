package com.dentflow.dto;

import java.time.LocalDate;

/** Nombre de rendez-vous actifs pour une journee : alimente les pastilles du calendrier. */
public record CalendarDayResponse(
        LocalDate date,
        long count
) {
}
