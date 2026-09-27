package com.dentflow.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Facture à laquelle appartient cette ligne
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Prestation concernée
    @ManyToOne
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    // Description enregistrée dans la facture
    private String description;

    // Exemple : 2 radios
    private Integer quantity = 1;

    // Prix d'une unité
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Prix total de cette ligne
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    public void calculateTotal() {

        if (unitPrice != null && quantity != null) {

            totalPrice = unitPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );
        }
    }

    @PrePersist
    @PreUpdate
    public void beforeSave() {
        calculateTotal();
    }
}
