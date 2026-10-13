package com.dentflow.service;

import com.dentflow.dto.AppointmentCancelRequest;
import com.dentflow.dto.AppointmentRequest;
import com.dentflow.dto.AppointmentResponse;
import com.dentflow.dto.AppointmentStatsResponse;
import com.dentflow.dto.CalendarDayResponse;
import com.dentflow.model.Appointment;
import com.dentflow.model.AppointmentStatus;
import com.dentflow.model.Dentist;
import com.dentflow.model.Patient;
import com.dentflow.model.Treatment;
import com.dentflow.repository.AppointmentRepository;
import com.dentflow.repository.DentistRepository;
import com.dentflow.repository.PatientRepository;
import com.dentflow.repository.TreatmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class AppointmentService {

    private static final int DEFAULT_DURATION_MINUTES = 30;

    /**
     * Demi-fenetre utilisee pour recuperer les rendez-vous candidats au chevauchement.
     * Large par rapport a la duree maximale autorisee (8 h) pour ne manquer aucun voisin.
     */
    private static final int OVERLAP_WINDOW_HOURS = 12;

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DentistRepository dentistRepository,
            TreatmentRepository treatmentRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.treatmentRepository = treatmentRepository;
    }

    /** Tous les filtres sont appliques en base par une requete unique. */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(Long patientId, Long dentistId, AppointmentStatus status) {
        return appointmentRepository.search(patientId, dentistId, status).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    /**
     * Pastilles du calendrier : nombre de rendez-vous actifs par jour sur un mois.
     * Le regroupement est fait par la base.
     */
    @Transactional(readOnly = true)
    public List<CalendarDayResponse> getCalendar(YearMonth month) {
        YearMonth target = month == null ? YearMonth.now() : month;

        return appointmentRepository.findDayCounts(
                target.atDay(1).atStartOfDay(),
                target.plusMonths(1).atDay(1).atStartOfDay()
        );
    }

    /** Agenda d'une journee, trie par heure croissante. */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDayAgenda(LocalDate day) {
        LocalDate target = day == null ? LocalDate.now() : day;

        return appointmentRepository
                .findDayDetailed(target.atStartOfDay(), target.plusDays(1).atStartOfDay())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    /** Compteurs du planning, calcules par la base et non par le client. */
    @Transactional(readOnly = true)
    public AppointmentStatsResponse getStats() {
        return new AppointmentStatsResponse(
                appointmentRepository.count(),
                appointmentRepository.countUpcoming(LocalDateTime.now()),
                appointmentRepository.countByStatus(AppointmentStatus.COMPLETED),
                appointmentRepository.countByStatus(AppointmentStatus.CANCELLED)
        );
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        return AppointmentResponse.from(findAppointment(id));
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient introuvable"));
        Dentist dentist = dentistRepository.findById(request.dentistId())
                .orElseThrow(() -> new EntityNotFoundException("Dentiste introuvable"));

        if (!patient.isActive()) {
            throw new IllegalStateException("Ce patient est archive : impossible de lui creer un rendez-vous");
        }
        if (!dentist.isActive()) {
            throw new IllegalStateException("Ce dentiste est desactive : impossible de lui creer un rendez-vous");
        }

        Treatment treatment = resolveTreatment(request.treatmentId());
        int duration = resolveDuration(request.durationMinutes(), treatment);

        ensureSlotIsFree(dentist.getId(), request.appointmentDate(), duration, null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setDurationMinutes(duration);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(trimToNull(request.reason()));
        appointment.setNotes(trimToNull(request.notes()));

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Un rendez-vous annule ne peut plus etre modifie");
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient introuvable"));
        Dentist dentist = dentistRepository.findById(request.dentistId())
                .orElseThrow(() -> new EntityNotFoundException("Dentiste introuvable"));

        Treatment treatment = resolveTreatment(request.treatmentId());
        int duration = resolveDuration(request.durationMinutes(), treatment);
        boolean dateChanged = !appointment.getAppointmentDate().equals(request.appointmentDate());

        ensureSlotIsFree(dentist.getId(), request.appointmentDate(), duration, id);

        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setDurationMinutes(duration);
        appointment.setReason(trimToNull(request.reason()));
        appointment.setNotes(trimToNull(request.notes()));

        if (dateChanged && appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.RESCHEDULED);
        }

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id, AppointmentCancelRequest request) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Ce rendez-vous est deja annule");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Un rendez-vous deja realise ne peut pas etre annule");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancellationReason(
                request == null ? null : trimToNull(request.cancellationReason())
        );

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long id) {
        Appointment appointment = findAppointment(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Un rendez-vous annule ne peut pas etre marque comme realise");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    /**
     * Refuse un creneau qui en chevauche un autre chez le meme dentiste.
     * La fenetre SQL ne fait que reduire les candidats : le test exact
     * [debut, fin[ contre [debut, fin[ est fait en Java, sans arithmetique
     * de date dependante du dialecte.
     */
    private void ensureSlotIsFree(Long dentistId, LocalDateTime start, int duration, Long excludedId) {
        LocalDateTime end = start.plusMinutes(duration);

        boolean overlaps = appointmentRepository
                .findActiveBetween(
                        dentistId,
                        start.minusHours(OVERLAP_WINDOW_HOURS),
                        end.plusHours(OVERLAP_WINDOW_HOURS)
                )
                .stream()
                .filter(existing -> !existing.getId().equals(excludedId))
                .anyMatch(existing -> existing.getAppointmentDate().isBefore(end)
                        && start.isBefore(existing.getEndDate()));

        if (overlaps) {
            throw new IllegalStateException(
                    "Ce dentiste a deja un rendez-vous sur ce creneau. Choisissez un autre horaire."
            );
        }
    }

    private Treatment resolveTreatment(Long treatmentId) {
        if (treatmentId == null) {
            return null;
        }
        return treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new EntityNotFoundException("Soin introuvable"));
    }

    private int resolveDuration(Integer requested, Treatment treatment) {
        if (requested != null) {
            return requested;
        }
        if (treatment != null && treatment.getDurationMinutes() != null && treatment.getDurationMinutes() > 0) {
            return treatment.getDurationMinutes();
        }
        return DEFAULT_DURATION_MINUTES;
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findDetailedById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous introuvable"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
