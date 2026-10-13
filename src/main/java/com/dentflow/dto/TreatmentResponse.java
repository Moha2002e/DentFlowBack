package com.dentflow.dto;

import com.dentflow.model.Treatment;

import java.math.BigDecimal;

public record TreatmentResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        boolean active
) {
    public static TreatmentResponse from(Treatment treatment) {
        return new TreatmentResponse(
                treatment.getId(),
                treatment.getName(),
                treatment.getDescription(),
                treatment.getPrice(),
                treatment.getDurationMinutes(),
                treatment.isActive()
        );
    }
}
