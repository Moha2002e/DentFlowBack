package com.dentflow.controller;

import com.dentflow.dto.TreatmentResponse;
import com.dentflow.service.TreatmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    /** Catalogue des soins actifs, utilise par le formulaire de rendez-vous. */
    @GetMapping
    public List<TreatmentResponse> getActiveTreatments() {
        return treatmentService.getActiveTreatments();
    }
}
