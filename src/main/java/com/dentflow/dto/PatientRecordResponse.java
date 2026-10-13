package com.dentflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Dossier patient : la fiche, son historique de rendez-vous et quelques compteurs. */
public record PatientRecordResponse(
        PatientResponse patient,
        List<AppointmentResponse> appointments,
        PatientRecordStats stats
) {
    public record PatientRecordStats(
            int totalAppointments,
            int completedAppointments,
            int cancelledAppointments,
            int upcomingAppointments,
            LocalDateTime lastVisit,
            LocalDateTime nextAppointment,
            BigDecimal totalBilled
    ) {
    }
}
