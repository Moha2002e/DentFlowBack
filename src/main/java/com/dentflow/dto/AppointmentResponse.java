package com.dentflow.dto;

import com.dentflow.model.Appointment;
import com.dentflow.model.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long dentistId,
        String dentistName,
        Long treatmentId,
        String treatmentName,
        BigDecimal treatmentPrice,
        LocalDateTime appointmentDate,
        LocalDateTime endDate,
        Integer durationMinutes,
        AppointmentStatus status,
        String reason,
        String notes,
        LocalDateTime cancelledAt,
        String cancellationReason,

        // Actions autorisees, decidees par le serveur a partir des memes regles
        // que celles appliquees par AppointmentService. Le front se contente
        // d'activer ou de masquer les boutons.
        boolean canEdit,
        boolean canCancel,
        boolean canComplete
) {
    public static AppointmentResponse from(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();
        boolean finished = status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED;

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment.getDentist().getId(),
                "Dr " + appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName(),
                appointment.getTreatment() == null ? null : appointment.getTreatment().getId(),
                appointment.getTreatment() == null ? null : appointment.getTreatment().getName(),
                appointment.getTreatment() == null ? null : appointment.getTreatment().getPrice(),
                appointment.getAppointmentDate(),
                appointment.getEndDate(),
                appointment.getDurationMinutes(),
                status,
                appointment.getReason(),
                appointment.getNotes(),
                appointment.getCancelledAt(),
                appointment.getCancellationReason(),
                status != AppointmentStatus.CANCELLED,
                !finished,
                !finished
        );
    }
}
