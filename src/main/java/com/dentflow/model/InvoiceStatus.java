package com.dentflow.model;

public enum InvoiceStatus {

    DRAFT,              // Facture créée mais pas encore envoyée
    SENT,               // Facture envoyée
    PARTIALLY_PAID,     // Une partie a été payée
    PAID,               // Facture entièrement payée
    OVERDUE,            // Date limite dépassée
    CANCELLED           // Facture annulée
}
