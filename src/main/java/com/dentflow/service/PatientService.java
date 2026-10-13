package com.dentflow.service;

import com.dentflow.dto.AppointmentResponse;
import com.dentflow.dto.PatientRecordResponse;
import com.dentflow.dto.PatientRequest;
import com.dentflow.dto.PatientResponse;
import com.dentflow.model.Appointment;
import com.dentflow.model.AppointmentStatus;
import com.dentflow.model.Patient;
import com.dentflow.repository.AppointmentRepository;
import com.dentflow.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /** Recherche et filtre actif sont delegues a la base. */
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients(String search, boolean activeOnly) {
        // Chaine vide = aucun filtre : le parametre ne doit jamais etre null.
        String term = StringUtils.hasText(search) ? search.trim() : "";

        return patientRepository.search(term, activeOnly).stream()
                .map(PatientResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        return PatientResponse.from(findPatient(id));
    }

    /** Dossier patient complet : fiche + historique des rendez-vous + compteurs. */
    @Transactional(readOnly = true)
    public PatientRecordResponse getPatientRecord(Long id) {
        Patient patient = findPatient(id);
        List<Appointment> appointments = appointmentRepository.findByPatientDetailed(id);
        LocalDateTime now = LocalDateTime.now();

        int completed = 0;
        int cancelled = 0;
        int upcoming = 0;
        LocalDateTime lastVisit = null;
        LocalDateTime nextAppointment = null;
        BigDecimal totalBilled = BigDecimal.ZERO;

        for (Appointment appointment : appointments) {
            switch (appointment.getStatus()) {
                case COMPLETED -> completed++;
                case CANCELLED -> cancelled++;
                default -> {
                }
            }

            boolean active = appointment.getStatus() != AppointmentStatus.CANCELLED;

            if (active && appointment.getAppointmentDate().isAfter(now)) {
                upcoming++;
            }

            if (appointment.getStatus() == AppointmentStatus.COMPLETED
                    && (lastVisit == null || appointment.getAppointmentDate().isAfter(lastVisit))) {
                lastVisit = appointment.getAppointmentDate();
            }

            if (active
                    && appointment.getAppointmentDate().isAfter(now)
                    && (nextAppointment == null || appointment.getAppointmentDate().isBefore(nextAppointment))) {
                nextAppointment = appointment.getAppointmentDate();
            }

            if (appointment.getStatus() == AppointmentStatus.COMPLETED
                    && appointment.getTreatment() != null
                    && appointment.getTreatment().getPrice() != null) {
                totalBilled = totalBilled.add(appointment.getTreatment().getPrice());
            }
        }

        List<AppointmentResponse> history = appointments.stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).reversed())
                .map(AppointmentResponse::from)
                .toList();

        return new PatientRecordResponse(
                PatientResponse.from(patient),
                history,
                new PatientRecordResponse.PatientRecordStats(
                        appointments.size(),
                        completed,
                        cancelled,
                        upcoming,
                        lastVisit,
                        nextAppointment,
                        totalBilled
                )
        );
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        String email = normalizeEmail(request.email());

        if (patientRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Un patient utilise deja cette adresse e-mail");
        }

        Patient patient = new Patient();
        apply(patient, request, email);
        patient.setActive(true);
        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = findPatient(id);
        String email = normalizeEmail(request.email());

        if (patientRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new IllegalStateException("Un patient utilise deja cette adresse e-mail");
        }

        apply(patient, request, email);
        return PatientResponse.from(patientRepository.save(patient));
    }

    /** Archivage : on ne supprime pas un patient qui porte un historique medical. */
    @Transactional
    public PatientResponse archivePatient(Long id) {
        Patient patient = findPatient(id);
        patient.setActive(false);
        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse restorePatient(Long id) {
        Patient patient = findPatient(id);
        patient.setActive(true);
        return PatientResponse.from(patientRepository.save(patient));
    }

    private void apply(Patient patient, PatientRequest request, String email) {
        patient.setFirstName(request.firstName().trim());
        patient.setLastName(request.lastName().trim());
        patient.setEmail(email);
        patient.setPhoneNumber(trimToNull(request.phoneNumber()));
        patient.setBirthDate(request.birthDate());
        patient.setNationalNumber(trimToNull(request.nationalNumber()));
        patient.setAddress(trimToNull(request.address()));
        patient.setCity(trimToNull(request.city()));
        patient.setPostalCode(trimToNull(request.postalCode()));
        patient.setIban(trimToNull(request.iban()));
        patient.setMedicalNotes(trimToNull(request.medicalNotes()));
    }

    private Patient findPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient introuvable"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
