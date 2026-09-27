package com.dentflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exemple : INV-2026-00001
    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    // Patient concerné par la facture
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Rendez-vous ayant créé cette facture
    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    // Date de création de la facture
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Date limite pour payer
    private LocalDate dueDate;

    // Statut de la facture
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    // Montant total
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Toutes les lignes de la facture
    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<InvoiceItem> items = new ArrayList<>();

    // Petite remarque éventuelle
    private String notes;

    @PrePersist
    public void beforeCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }
    }
}
