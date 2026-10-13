package com.dentflow.config;

import com.dentflow.model.Appointment;
import com.dentflow.model.AppointmentStatus;
import com.dentflow.model.Dentist;
import com.dentflow.model.Patient;
import com.dentflow.model.Role;
import com.dentflow.model.Treatment;
import com.dentflow.model.User;
import com.dentflow.repository.AppointmentRepository;
import com.dentflow.repository.DentistRepository;
import com.dentflow.repository.PatientRepository;
import com.dentflow.repository.TreatmentRepository;
import com.dentflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Jeu de donnees de developpement : compte administrateur, soins, dentistes,
 * patients et quelques rendez-vous. Chaque bloc ne s'execute que si sa table est
 * vide, donc relancer l'application ne cree jamais de doublon.
 * Desactivable avec app.seed.enabled=false.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DataSeeder(
            UserRepository userRepository,
            PatientRepository patientRepository,
            DentistRepository dentistRepository,
            TreatmentRepository treatmentRepository,
            AppointmentRepository appointmentRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean enabled
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.treatmentRepository = treatmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        seedAdmin();
        seedTreatments();
        seedDentists();
        seedPatients();
        seedAppointments();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmailIgnoreCase("admin@admin.com")) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setFirstName("Admin");
        admin.setLastName("DentFlow");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        log.info("Compte administrateur de demonstration cree : admin@admin.com");
    }

    private void seedTreatments() {
        if (treatmentRepository.count() > 0) {
            return;
        }

        treatmentRepository.saveAll(List.of(
                treatment("Consultation de controle", "Examen complet et bilan bucco-dentaire", "45.00", 30),
                treatment("Detartrage", "Nettoyage et polissage des dents", "85.00", 45),
                treatment("Soin de carie", "Obturation composite", "120.00", 60),
                treatment("Extraction simple", "Extraction d'une dent sans complication", "150.00", 45),
                treatment("Traitement de racine", "Devitalisation et obturation canalaire", "380.00", 90),
                treatment("Pose de couronne", "Couronne ceramique sur dent preparee", "650.00", 90),
                treatment("Blanchiment", "Eclaircissement au fauteuil", "290.00", 60),
                treatment("Radiographie panoramique", "Cliche panoramique complet", "65.00", 15)
        ));

        log.info("Catalogue de soins de demonstration cree");
    }

    private Treatment treatment(String name, String description, String price, int durationMinutes) {
        Treatment treatment = new Treatment();
        treatment.setName(name);
        treatment.setDescription(description);
        treatment.setPrice(new BigDecimal(price));
        treatment.setDurationMinutes(durationMinutes);
        treatment.setActive(true);
        return treatment;
    }

    private void seedDentists() {
        if (dentistRepository.count() > 0) {
            return;
        }

        dentistRepository.saveAll(List.of(
                dentist("Camille", "Dubois", "camille.dubois@dentflow.be", "+32 471 11 22 33",
                        "Dentisterie generale", "10123456", "Rue de la Station 14", "Namur", "5000"),
                dentist("Youssef", "Benali", "youssef.benali@dentflow.be", "+32 472 44 55 66",
                        "Orthodontie", "10234567", "Avenue Louise 210", "Bruxelles", "1050"),
                dentist("Marie", "Lefevre", "marie.lefevre@dentflow.be", "+32 473 77 88 99",
                        "Parodontologie", "10345678", "Chaussee de Liege 7", "Liege", "4000"),
                dentist("Thomas", "Vermeulen", "thomas.vermeulen@dentflow.be", "+32 474 00 11 22",
                        "Implantologie", "10456789", "Grote Markt 3", "Gand", "9000")
        ));

        log.info("Dentistes de demonstration crees");
    }

    private Dentist dentist(
            String firstName, String lastName, String email, String phone,
            String specialization, String inami, String address, String city, String postalCode
    ) {
        Dentist dentist = new Dentist();
        dentist.setFirstName(firstName);
        dentist.setLastName(lastName);
        dentist.setEmail(email);
        dentist.setPhoneNumber(phone);
        dentist.setSpecialization(specialization);
        dentist.setInami(inami);
        dentist.setAddress(address);
        dentist.setCity(city);
        dentist.setPostalCode(postalCode);
        dentist.setActive(true);
        return dentist;
    }

    private void seedPatients() {
        if (patientRepository.count() > 0) {
            return;
        }

        patientRepository.saveAll(List.of(
                patient("Lucas", "Martin", "lucas.martin@example.com", "+32 475 12 34 56",
                        LocalDate.of(1988, 3, 14), "Rue des Tilleuls 22", "Namur", "5000",
                        "Allergie a la penicilline. Bruxisme nocturne."),
                patient("Emma", "Lambert", "emma.lambert@example.com", "+32 476 23 45 67",
                        LocalDate.of(1995, 7, 2), "Avenue des Arts 8", "Bruxelles", "1000",
                        "Aucun antecedent particulier."),
                patient("Noah", "Peeters", "noah.peeters@example.com", "+32 477 34 56 78",
                        LocalDate.of(2012, 11, 23), "Kerkstraat 45", "Gand", "9000",
                        "Patient mineur. Suivi orthodontique en cours."),
                patient("Sofia", "Garcia", "sofia.garcia@example.com", "+32 478 45 67 89",
                        LocalDate.of(1979, 1, 9), "Boulevard d Avroy 101", "Liege", "4000",
                        "Hypertension traitee. Anesthesie sans adrenaline."),
                patient("Hugo", "Leroy", "hugo.leroy@example.com", "+32 479 56 78 90",
                        LocalDate.of(2001, 5, 30), "Rue Haute 67", "Charleroi", "6000",
                        "Sensibilite dentinaire importante."),
                patient("Chloe", "Simon", "chloe.simon@example.com", "+32 470 67 89 01",
                        LocalDate.of(1992, 9, 17), "Place du Marche 3", "Mons", "7000",
                        "Grossesse en cours : eviter les radiographies.")
        ));

        log.info("Patients de demonstration crees");
    }

    private Patient patient(
            String firstName, String lastName, String email, String phone, LocalDate birthDate,
            String address, String city, String postalCode, String medicalNotes
    ) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setEmail(email);
        patient.setPhoneNumber(phone);
        patient.setBirthDate(birthDate);
        patient.setAddress(address);
        patient.setCity(city);
        patient.setPostalCode(postalCode);
        patient.setMedicalNotes(medicalNotes);
        patient.setActive(true);
        return patient;
    }

    private void seedAppointments() {
        if (appointmentRepository.count() > 0) {
            return;
        }

        // Recherche par identifiant metier : les listes triees ne garantissent pas
        // l'ordre d'insertion, et indexer dessus associait des motifs aux mauvais soins.
        Patient lucas = patientByEmail("lucas.martin@example.com");
        Patient emma = patientByEmail("emma.lambert@example.com");
        Patient noah = patientByEmail("noah.peeters@example.com");
        Patient sofia = patientByEmail("sofia.garcia@example.com");
        Patient hugo = patientByEmail("hugo.leroy@example.com");
        Patient chloe = patientByEmail("chloe.simon@example.com");

        Dentist camille = dentistByEmail("camille.dubois@dentflow.be");
        Dentist youssef = dentistByEmail("youssef.benali@dentflow.be");
        Dentist marie = dentistByEmail("marie.lefevre@dentflow.be");
        Dentist thomas = dentistByEmail("thomas.vermeulen@dentflow.be");

        Treatment controle = treatmentByName("Consultation de controle");
        Treatment detartrage = treatmentByName("Detartrage");
        Treatment carie = treatmentByName("Soin de carie");
        Treatment extraction = treatmentByName("Extraction simple");
        Treatment racine = treatmentByName("Traitement de racine");
        Treatment couronne = treatmentByName("Pose de couronne");
        Treatment panoramique = treatmentByName("Radiographie panoramique");

        LocalDateTime today = LocalDate.now().atTime(LocalTime.of(9, 0));

        appointmentRepository.saveAll(List.of(
                // Realises : alimentent l'historique du dossier patient.
                appointment(lucas, camille, controle,
                        today.minusDays(40), AppointmentStatus.COMPLETED, "Controle annuel", null),
                appointment(lucas, camille, detartrage,
                        today.minusDays(18).plusHours(2), AppointmentStatus.COMPLETED, "Detartrage", null),
                appointment(emma, youssef, carie,
                        today.minusDays(25).plusHours(4), AppointmentStatus.COMPLETED, "Carie molaire", null),
                appointment(noah, marie, controle,
                        today.minusDays(10).plusHours(1), AppointmentStatus.COMPLETED, "Premiere visite", null),

                // Annule : montre le motif d'annulation dans le dossier.
                cancelled(sofia, youssef, extraction,
                        today.minusDays(5).plusHours(3), "Patient souffrant, reporte a sa demande"),

                // A venir : alimentent le planning.
                appointment(emma, camille, racine,
                        today.plusDays(2).plusHours(1), AppointmentStatus.SCHEDULED, "Traitement de racine", null),
                appointment(noah, marie, detartrage,
                        today.plusDays(3).plusHours(3), AppointmentStatus.SCHEDULED, "Detartrage semestriel", null),
                appointment(hugo, thomas, couronne,
                        today.plusDays(6).plusHours(2), AppointmentStatus.SCHEDULED, "Pose de couronne", null),
                appointment(chloe, camille, controle,
                        today.plusDays(8), AppointmentStatus.SCHEDULED, "Controle de routine", null),
                appointment(sofia, thomas, panoramique,
                        today.plusDays(11).plusHours(5), AppointmentStatus.SCHEDULED, "Panoramique de controle", null)
        ));

        log.info("Rendez-vous de demonstration crees");
    }

    private Patient patientByEmail(String email) {
        return patientRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .filter(patient -> patient.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Patient de demonstration absent : " + email));
    }

    private Dentist dentistByEmail(String email) {
        return dentistRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .filter(dentist -> dentist.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Dentiste de demonstration absent : " + email));
    }

    private Treatment treatmentByName(String name) {
        return treatmentRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .filter(treatment -> treatment.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Soin de demonstration absent : " + name));
    }

    private Appointment appointment(
            Patient patient, Dentist dentist, Treatment treatment,
            LocalDateTime date, AppointmentStatus status, String reason, String notes
    ) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        appointment.setAppointmentDate(date);
        appointment.setDurationMinutes(
                treatment.getDurationMinutes() == null ? 30 : treatment.getDurationMinutes()
        );
        appointment.setStatus(status);
        appointment.setReason(reason);
        appointment.setNotes(notes);
        return appointment;
    }

    private Appointment cancelled(
            Patient patient, Dentist dentist, Treatment treatment,
            LocalDateTime date, String cancellationReason
    ) {
        Appointment appointment = appointment(
                patient, dentist, treatment, date, AppointmentStatus.CANCELLED, "Extraction prevue", null
        );
        appointment.setCancelledAt(date.minusDays(1));
        appointment.setCancellationReason(cancellationReason);
        return appointment;
    }
}
