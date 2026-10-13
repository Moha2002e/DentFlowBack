package com.dentflow.controller;

import com.dentflow.dto.PatientRecordResponse;
import com.dentflow.dto.PatientRequest;
import com.dentflow.dto.PatientResponse;
import com.dentflow.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasAnyRole('ADMIN', 'DENTIST')")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<PatientResponse> getAllPatients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        return patientService.getAllPatients(search, activeOnly);
    }

    @GetMapping("/{id}")
    public PatientResponse getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }

    /** Dossier patient : fiche, historique des rendez-vous et compteurs. */
    @GetMapping("/{id}/record")
    public PatientRecordResponse getPatientRecord(@PathVariable Long id) {
        return patientService.getPatientRecord(id);
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(201).body(patientService.createPatient(request));
    }

    @PutMapping("/{id}")
    public PatientResponse updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request
    ) {
        return patientService.updatePatient(id, request);
    }

    @DeleteMapping("/{id}")
    public PatientResponse archivePatient(@PathVariable Long id) {
        return patientService.archivePatient(id);
    }

    @PostMapping("/{id}/restore")
    public PatientResponse restorePatient(@PathVariable Long id) {
        return patientService.restorePatient(id);
    }
}
