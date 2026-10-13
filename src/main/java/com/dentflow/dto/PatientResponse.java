package com.dentflow.dto;

import com.dentflow.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        LocalDate birthDate,
        Integer age,
        String nationalNumber,
        String address,
        String city,
        String postalCode,
        String iban,
        String medicalNotes,
        boolean active,
        LocalDateTime createdAt
) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getFirstName() + " " + patient.getLastName(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getBirthDate(),
                age(patient.getBirthDate()),
                patient.getNationalNumber(),
                patient.getAddress(),
                patient.getCity(),
                patient.getPostalCode(),
                patient.getIban(),
                patient.getMedicalNotes(),
                patient.isActive(),
                patient.getCreatedAt()
        );
    }

    private static Integer age(LocalDate birthDate) {
        return birthDate == null
                ? null
                : java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
}
