package com.dentflow.repository;

import com.dentflow.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Patient> findAllByOrderByLastNameAscFirstNameAsc();

    /**
     * Recherche unique : terme libre et filtre actif sont appliques en base, le
     * client ne filtre rien. Le terme n'est jamais null (chaine vide = tout),
     * car PostgreSQL ne peut pas inferer le type d'un parametre null place dans
     * concat() et echouerait sur "function lower(bytea) does not exist".
     */
    @Query("""
            select p from Patient p
            where (lower(p.firstName) like lower(concat('%', :term, '%'))
                   or lower(p.lastName) like lower(concat('%', :term, '%'))
                   or lower(p.email) like lower(concat('%', :term, '%')))
              and (:activeOnly = false or p.active = true)
            order by p.lastName asc, p.firstName asc
            """)
    List<Patient> search(@Param("term") String term, @Param("activeOnly") boolean activeOnly);
}
