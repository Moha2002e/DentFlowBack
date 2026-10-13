package com.dentflow.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Grille complete d'un mois, prete a afficher : le serveur calcule les semaines,
 * les jours deborant sur les mois voisins et repartit les rendez-vous dans chaque
 * case. Le client ne fait que boucler dessus.
 */
public record CalendarMonthResponse(
        String month,
        String label,
        String previousMonth,
        String nextMonth,
        List<String> dayHeaders,
        List<CalendarWeek> weeks,
        int totalAppointments
) {
    public record CalendarWeek(
            int weekNumber,
            List<CalendarCell> days
    ) {
    }

    public record CalendarCell(
            LocalDate date,
            int dayOfMonth,
            boolean inCurrentMonth,
            boolean today,
            boolean weekend,
            List<AppointmentResponse> appointments
    ) {
    }
}
