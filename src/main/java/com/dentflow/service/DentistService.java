package com.dentflow.service;

import com.dentflow.dto.DentistRequest;
import com.dentflow.dto.DentistResponse;
import com.dentflow.model.Dentist;
import com.dentflow.repository.DentistRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class DentistService {

    private final DentistRepository dentistRepository;

    public DentistService(DentistRepository dentistRepository) {
        this.dentistRepository = dentistRepository;
    }

    /** Recherche et filtre actif sont delegues a la base. */
    @Transactional(readOnly = true)
    public List<DentistResponse> getAllDentists(String search, boolean activeOnly) {
        // Chaine vide = aucun filtre : le parametre ne doit jamais etre null.
        String term = StringUtils.hasText(search) ? search.trim() : "";

        return dentistRepository.search(term, activeOnly).stream()
                .map(DentistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DentistResponse getDentistById(Long id) {
        return DentistResponse.from(findDentist(id));
    }

    @Transactional
    public DentistResponse createDentist(DentistRequest request) {
        String email = normalizeEmail(request.email());
        String inami = trimToNull(request.inami());

        if (dentistRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Un dentiste utilise deja cette adresse e-mail");
        }
        if (inami != null && dentistRepository.existsByInamiIgnoreCase(inami)) {
            throw new IllegalStateException("Ce numero INAMI est deja enregistre");
        }

        Dentist dentist = new Dentist();
        apply(dentist, request, email, inami);
        dentist.setActive(true);
        return DentistResponse.from(dentistRepository.save(dentist));
    }

    @Transactional
    public DentistResponse updateDentist(Long id, DentistRequest request) {
        Dentist dentist = findDentist(id);
        String email = normalizeEmail(request.email());
        String inami = trimToNull(request.inami());

        if (dentistRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new IllegalStateException("Un dentiste utilise deja cette adresse e-mail");
        }
        if (inami != null && dentistRepository.existsByInamiIgnoreCaseAndIdNot(inami, id)) {
            throw new IllegalStateException("Ce numero INAMI est deja enregistre");
        }

        apply(dentist, request, email, inami);
        return DentistResponse.from(dentistRepository.save(dentist));
    }

    /** Desactivation : le dentiste disparait des plannings mais garde son historique. */
    @Transactional
    public DentistResponse deactivateDentist(Long id) {
        Dentist dentist = findDentist(id);
        dentist.setActive(false);
        return DentistResponse.from(dentistRepository.save(dentist));
    }

    @Transactional
    public DentistResponse activateDentist(Long id) {
        Dentist dentist = findDentist(id);
        dentist.setActive(true);
        return DentistResponse.from(dentistRepository.save(dentist));
    }

    private void apply(Dentist dentist, DentistRequest request, String email, String inami) {
        dentist.setFirstName(request.firstName().trim());
        dentist.setLastName(request.lastName().trim());
        dentist.setEmail(email);
        dentist.setPhoneNumber(trimToNull(request.phoneNumber()));
        dentist.setSpecialization(trimToNull(request.specialization()));
        dentist.setInami(inami);
        dentist.setAddress(trimToNull(request.address()));
        dentist.setCity(trimToNull(request.city()));
        dentist.setPostalCode(trimToNull(request.postalCode()));
        dentist.setIban(trimToNull(request.iban()));
    }

    private Dentist findDentist(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dentiste introuvable"));
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
