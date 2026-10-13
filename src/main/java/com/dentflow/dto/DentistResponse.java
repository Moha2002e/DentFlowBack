package com.dentflow.dto;

import com.dentflow.model.Dentist;

public record DentistResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        String specialization,
        String inami,
        String address,
        String city,
        String postalCode,
        String iban,
        boolean active
) {
    public static DentistResponse from(Dentist dentist) {
        return new DentistResponse(
                dentist.getId(),
                dentist.getFirstName(),
                dentist.getLastName(),
                "Dr " + dentist.getFirstName() + " " + dentist.getLastName(),
                dentist.getEmail(),
                dentist.getPhoneNumber(),
                dentist.getSpecialization(),
                dentist.getInami(),
                dentist.getAddress(),
                dentist.getCity(),
                dentist.getPostalCode(),
                dentist.getIban(),
                dentist.isActive()
        );
    }
}
