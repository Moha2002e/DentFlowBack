package com.dentflow.service;

import com.dentflow.dto.TreatmentResponse;
import com.dentflow.repository.TreatmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    public TreatmentService(TreatmentRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }

    @Transactional(readOnly = true)
    public List<TreatmentResponse> getActiveTreatments() {
        return treatmentRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(TreatmentResponse::from)
                .toList();
    }
}
