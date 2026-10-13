package com.dentflow.repository;

import com.dentflow.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findAllByActiveTrueOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
