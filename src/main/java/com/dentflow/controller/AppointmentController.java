package com.dentflow.controller;

import com.dentflow.dto.AppointmentCancelRequest;
import com.dentflow.dto.AppointmentRequest;
import com.dentflow.dto.AppointmentResponse;
import com.dentflow.dto.AppointmentStatsResponse;
import com.dentflow.dto.CalendarDayResponse;
import com.dentflow.model.AppointmentStatus;
import com.dentflow.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTIST')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> getAllAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) AppointmentStatus status
    ) {
        return appointmentService.getAllAppointments(patientId, dentistId, status);
    }

    /** Compteurs du planning calcules par la base. */
    @GetMapping("/stats")
    public AppointmentStatsResponse getStats() {
        return appointmentService.getStats();
    }

    /**
     * Pastilles du calendrier : nombre de rendez-vous actifs par jour du mois.
     * Mois au format ISO (2026-10) ; mois courant si absent.
     */
    @GetMapping("/calendar")
    public List<CalendarDayResponse> getCalendar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return appointmentService.getCalendar(month);
    }

    /** Agenda d'une journee (jour courant si absent). */
    @GetMapping("/day")
    public List<AppointmentResponse> getDayAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentService.getDayAgenda(date);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request
    ) {
        return ResponseEntity.status(201).body(appointmentService.createAppointment(request));
    }

    @PutMapping("/{id}")
    public AppointmentResponse updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request
    ) {
        return appointmentService.updateAppointment(id, request);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid AppointmentCancelRequest request
    ) {
        return appointmentService.cancelAppointment(id, request);
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse completeAppointment(@PathVariable Long id) {
        return appointmentService.completeAppointment(id);
    }
}
