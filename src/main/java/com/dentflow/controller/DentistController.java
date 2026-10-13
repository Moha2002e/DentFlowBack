package com.dentflow.controller;

import com.dentflow.dto.DentistRequest;
import com.dentflow.dto.DentistResponse;
import com.dentflow.service.DentistService;
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
@RequestMapping("/api/dentists")
public class DentistController {

    private final DentistService dentistService;

    public DentistController(DentistService dentistService) {
        this.dentistService = dentistService;
    }

    /** Lecture ouverte a tout utilisateur connecte : la prise de rendez-vous en a besoin. */
    @GetMapping
    public List<DentistResponse> getAllDentists(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        return dentistService.getAllDentists(search, activeOnly);
    }

    @GetMapping("/{id}")
    public DentistResponse getDentistById(@PathVariable Long id) {
        return dentistService.getDentistById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DentistResponse> createDentist(@Valid @RequestBody DentistRequest request) {
        return ResponseEntity.status(201).body(dentistService.createDentist(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DentistResponse updateDentist(
            @PathVariable Long id,
            @Valid @RequestBody DentistRequest request
    ) {
        return dentistService.updateDentist(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DentistResponse deactivateDentist(@PathVariable Long id) {
        return dentistService.deactivateDentist(id);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public DentistResponse activateDentist(@PathVariable Long id) {
        return dentistService.activateDentist(id);
    }
}
