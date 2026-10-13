package com.dentflow.repository;

import com.dentflow.model.Appointment;
import com.dentflow.dto.CalendarDayResponse;
import com.dentflow.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Recherche unique du planning : patient, dentiste et statut sont tous
     * optionnels et filtres en base. Rien n'est filtre en memoire ni cote client.
     */
    @Query("""
            select a from Appointment a
            join fetch a.patient
            join fetch a.dentist
            left join fetch a.treatment
            where (:patientId is null or a.patient.id = :patientId)
              and (:dentistId is null or a.dentist.id = :dentistId)
              and (:status is null or a.status = :status)
            order by a.appointmentDate desc
            """)
    List<Appointment> search(
            @Param("patientId") Long patientId,
            @Param("dentistId") Long dentistId,
            @Param("status") AppointmentStatus status
    );

    @Query("""
            select a from Appointment a
            join fetch a.patient
            join fetch a.dentist
            left join fetch a.treatment
            where a.id = :id
            """)
    Optional<Appointment> findDetailedById(@Param("id") Long id);

    /** Agenda d'une journee, trie par heure croissante. */
    @Query("""
            select a from Appointment a
            join fetch a.patient
            join fetch a.dentist
            left join fetch a.treatment
            where a.appointmentDate >= :dayStart
              and a.appointmentDate < :dayEnd
            order by a.appointmentDate asc
            """)
    List<Appointment> findDayDetailed(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    /**
     * Nombre de rendez-vous actifs par journee sur une periode : les pastilles du
     * calendrier. Le regroupement est fait par la base, pas par le client.
     */
    @Query("""
            select new com.dentflow.dto.CalendarDayResponse(
                       cast(a.appointmentDate as LocalDate), count(a))
            from Appointment a
            where a.status <> com.dentflow.model.AppointmentStatus.CANCELLED
              and a.appointmentDate >= :start
              and a.appointmentDate < :end
            group by cast(a.appointmentDate as LocalDate)
            order by cast(a.appointmentDate as LocalDate)
            """)
    List<CalendarDayResponse> findDayCounts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select a from Appointment a
            join fetch a.patient
            join fetch a.dentist
            left join fetch a.treatment
            where a.patient.id = :patientId
            order by a.appointmentDate desc
            """)
    List<Appointment> findByPatientDetailed(@Param("patientId") Long patientId);

    /**
     * Rendez-vous actifs d'un dentiste commencant dans une fenetre donnee.
     * Le chevauchement exact est calcule en Java a partir de la duree de chaque
     * rendez-vous : on evite ainsi toute arithmetique de date dependante du dialecte.
     */
    @Query("""
            select a from Appointment a
            where a.dentist.id = :dentistId
              and a.status <> com.dentflow.model.AppointmentStatus.CANCELLED
              and a.appointmentDate >= :windowStart
              and a.appointmentDate < :windowEnd
            """)
    List<Appointment> findActiveBetween(
            @Param("dentistId") Long dentistId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );

    long countByStatus(AppointmentStatus status);

    /** Rendez-vous non annules encore a venir : compteur du planning. */
    @Query("""
            select count(a) from Appointment a
            where a.status <> com.dentflow.model.AppointmentStatus.CANCELLED
              and a.appointmentDate > :from
            """)
    long countUpcoming(@Param("from") LocalDateTime from);

    boolean existsByPatientId(Long patientId);

    boolean existsByDentistId(Long dentistId);

    boolean existsByTreatmentId(Long treatmentId);
}
