package com.dentflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Facture payée
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Montant payé
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Moyen de paiement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Date du paiement
    @Column(nullable = false)
    private LocalDateTime paymentDate;

    // Référence bancaire éventuellement
    private String reference;

    // Remarque
    private String notes;

    @PrePersist
    public void beforeCreate() {

        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }
}
