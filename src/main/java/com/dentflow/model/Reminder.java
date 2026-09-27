package com.dentflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Facture concernée
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Date prévue pour envoyer le rappel
    private LocalDateTime scheduledAt;

    // Date réelle d'envoi
    private LocalDateTime sentAt;

    // Statut du rappel
    @Enumerated(EnumType.STRING)
    private ReminderStatus status = ReminderStatus.PENDING;

    // Nombre de rappels déjà effectués
    private Integer attemptNumber = 1;

    // Sujet du message
    private String subject;

    // Contenu du rappel
    @Column(length = 2000)
    private String message;
}
