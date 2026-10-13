package com.dentflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    @ManyToOne
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    /** Duree reelle du creneau : reprise du soin par defaut, surchargeable. */
    @Column(nullable = false)
    private Integer durationMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    private String reason;

    @Column(length = 2000)
    private String notes;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void beforeCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AppointmentStatus.SCHEDULED;
        }
        if (durationMinutes == null) {
            durationMinutes = 30;
        }
    }

    /** Fin du creneau, utilisee pour detecter les chevauchements. */
    public LocalDateTime getEndDate() {
        return appointmentDate == null
                ? null
                : appointmentDate.plusMinutes(durationMinutes == null ? 30 : durationMinutes);
    }
}
