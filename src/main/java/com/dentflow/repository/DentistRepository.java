package com.dentflow.repository;

import com.dentflow.model.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DentistRepository extends JpaRepository<Dentist, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByInamiIgnoreCase(String inami);

    boolean existsByInamiIgnoreCaseAndIdNot(String inami, Long id);

    List<Dentist> findAllByOrderByLastNameAscFirstNameAsc();

    /**
     * Recherche unique : terme libre et filtre actif sont appliques en base.
     * Le terme n'est jamais null (chaine vide = tout) : PostgreSQL ne peut pas
     * inferer le type d'un parametre null place dans concat().
     */
    @Query("""
            select d from Dentist d
            where (lower(d.firstName) like lower(concat('%', :term, '%'))
                   or lower(d.lastName) like lower(concat('%', :term, '%'))
                   or lower(d.email) like lower(concat('%', :term, '%'))
                   or lower(coalesce(d.specialization, '')) like lower(concat('%', :term, '%')))
              and (:activeOnly = false or d.active = true)
            order by d.lastName asc, d.firstName asc
            """)
    List<Dentist> search(@Param("term") String term, @Param("activeOnly") boolean activeOnly);
}
