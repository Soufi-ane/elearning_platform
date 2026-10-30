package com.elearn.api.config;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.elearn.api.entity.*;
import com.elearn.api.repository.*;
import com.elearn.api.entity.Module;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
  private final UserRepository userRepository;
  private final ModuleRepository moduleRepository;
  private final ElementRepository elementRepository;
  private final DayPlanRepository dayPlanRepository;
  private final CampusRepository campusRepository;
  private final RoomRepository roomRepository;
  private final PasswordEncoder passwordEncoder;
  private final AbsenceRepository absenceRepository;
  private final ResultRepository resultRepository;
  private final RequestRepository requestRepository;

  @Value("${ENVIRONMENT}")
  private String environment;

  @Override
  public void run(String... args) throws Exception {
    if (args.length > 0 && "seed".equalsIgnoreCase(args[0])) {
      if (!"DEV".equalsIgnoreCase(environment) && userRepository.count() > 0) {
        System.out.println("Database already contains data. Skipping seeding.");
        return;
      }

      System.out.println("Seeding data ...");
      dayPlanRepository.deleteAllInBatch();
      absenceRepository.deleteAllInBatch();
      resultRepository.deleteAllInBatch();
      elementRepository.deleteAllInBatch();
      requestRepository.deleteAllInBatch();
      roomRepository.deleteAllInBatch();
      moduleRepository.deleteAllInBatch();
      userRepository.deleteAllInBatch();
      campusRepository.deleteAllInBatch();

      List<User> users = seedUsers();
      List<Module> modules = seedModules();
      List<Campus> campuses = seedCampuses();
      List<Element> elements = seedElements(modules, users);
      List<Room> rooms = seedRooms(campuses);

      seedResults(elements, users);
      seedTimeTables(elements, rooms);
      seedAbsence(users, elements);
      seedRequests(users);

      System.out.println("Seed complete!");
    }
  }

  private List<User> seedUsers() {
    List<User> users = List.of(
      new User("Admin", "System", "admin", null, "admin@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1990, 5, 15),
        Role.ADMIN, null, 0, 0
      ),
      new User("Soufiane", "Jaber", "soufiane", DepartmentName.GENIE_INFORMATIQUE,
        "soufianejb@mail.ma", passwordEncoder.encode("12341234"), LocalDate.of(2005, 11, 12),
        Role.STUDENT, StudyMode.HYBRID, 1, 1
      ),
      new User("Sara", "Alaoui", "sara-a", DepartmentName.GENIE_CIVIL, "sara.alaoui@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(2002, 10, 10),
        Role.STUDENT, StudyMode.ON_SITE, 2, 2
      ),
      new User("Omar", "Fassi", "omar-f", DepartmentName.GENIE_INFORMATIQUE, "omar.fassi@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(2004, 3, 22),
        Role.STUDENT, StudyMode.ON_SITE, 1, 1
      ),
      new User("Lina", "Idrissi", "lina-i", DepartmentName.GENIE_INFORMATIQUE, "lina.idrissi@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(2003, 7, 8),
        Role.STUDENT, StudyMode.HYBRID, 3, 2
      ),
      new User("Mehdi", "Bennani", "mehdi-b", DepartmentName.GENIE_INFORMATIQUE, "mehdi.bennani@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(2004, 12, 1),
        Role.STUDENT, StudyMode.ON_SITE, 1, 1
      ),
      new User("Driss", "Mansouri", "dmansouri", DepartmentName.GENIE_INFORMATIQUE, "driss.m@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1985, 1, 5), 
        Role.TEACHER, null, 0, 0
      ),
      new User("Hassan", "Zaki", "hzaki", DepartmentName.GENIE_INFORMATIQUE, "h.zaki@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1985, 1, 5),
        Role.TEACHER, null, 0 , 0
      ),
      new User("Nadia", "Amrani", "namrani", DepartmentName.GENIE_INFORMATIQUE, "n.amrani@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1978, 9, 14),
        Role.TEACHER, null, 0, 0
      ),
      new User("Youssef", "Kabbaj", "ykabbaj", DepartmentName.GENIE_INFORMATIQUE, "y.kabbaj@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1982, 4, 30),
        Role.TEACHER, null, 0, 0
      ),
      new User("Fatima", "Zahra", "fzahra", DepartmentName.GENIE_INFORMATIQUE, "f.zahra@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1975, 11, 22),
        Role.TEACHER, null, 0, 0
      ),
      new User("Karim", "Tazi", "ktazi", DepartmentName.GENIE_INFORMATIQUE, "k.tazi@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1988, 2, 18),
        Role.TEACHER, null, 0, 0
      )
    );
    return userRepository.saveAll(users);
  }

  private List<Module> seedModules() {
    List<Module> modules = List.of(
      // Semester 1
      new Module("Mathématiques pour l'Ingénieur", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Algorithmique et Programmation C", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Architecture et Systèmes d'Exploitation", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Langues et Communication 1", 1, DepartmentName.GENIE_INFORMATIQUE),

      // Semester 2
      new Module("Bases de Données Relationnelles", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Réseaux Informatiques", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Programmation Orientée Objet", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Développement Web", 2, DepartmentName.GENIE_INFORMATIQUE),

      // Semester 3
      new Module("Génie Logiciel et UML", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Technologies Java et JEE", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Développement Mobile", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Systèmes d'Information", 3, DepartmentName.GENIE_INFORMATIQUE)
    );
    return moduleRepository.saveAll(modules);
  }

  private List<Element> seedElements(List<Module> modules, List<User> users) {
    List<Element> elements = List.of(
      // S1 Elements
      new Element("Analyse Mathématique", modules.get(0), users.get(6)),
      new Element("Algèbre Linéaire", modules.get(0), users.get(6)),
      new Element("Algorithmique Avancée", modules.get(1), users.get(7)),
      new Element("Programmation C", modules.get(1), users.get(7)),
      new Element("Architecture des Ordinateurs", modules.get(2), users.get(8)),
      new Element("Systèmes d'Exploitation", modules.get(2), users.get(8)),
      new Element("Français Professionnel", modules.get(3), users.get(9)),
      new Element("Anglais Technique", modules.get(3), users.get(9)),

      // S2 Elements
      new Element("Conception des BDD", modules.get(4), users.get(10)),
      new Element("SQL et PL/SQL", modules.get(4), users.get(10)),
      new Element("Réseaux Locaux (LAN)", modules.get(5), users.get(11)),
      new Element("Réseaux Étendus (WAN)", modules.get(5), users.get(11)),
      new Element("Java Fondamentaux", modules.get(6), users.get(7)),
      new Element("C++ Avancé", modules.get(6), users.get(7)),
      new Element("Frontend (HTML/CSS/JS)", modules.get(7), users.get(8)),
      new Element("Backend (PHP/Node.js)", modules.get(7), users.get(8)),

      // S3 Elements
      new Element("Modélisation UML", modules.get(8), users.get(9)),
      new Element("Design Patterns", modules.get(8), users.get(9)),
      new Element("Spring Boot", modules.get(9), users.get(10)),
      new Element("Architecture Microservices", modules.get(9), users.get(10)),
      new Element("Android Natif", modules.get(10), users.get(11)),
      new Element("Flutter", modules.get(10), users.get(11)),
      new Element("Merise", modules.get(11), users.get(6)),
      new Element("Gouvernance IT", modules.get(11), users.get(6))
    );
    return elementRepository.saveAll(elements);
  }

  private List<Campus> seedCampuses() {
    List<Campus> campuses = List.of(
      new Campus("Agdal 1", "22 Av. Omar Ibn Al Khattab, Rabat 10090"),
      new Campus("Medina 1", "98 Av. Allal Ben Abdellah, Rabat 10000"),
      new Campus("Technopolis", "Rabat Shore, Sala Al Jadida 11100")
    );
    return campusRepository.saveAll(campuses);
  }

  private List<Room> seedRooms(List<Campus> campuses) {
    List<Room> rooms = List.of(
      new Room("TP2", 35, 3, RoomType.TP, campuses.get(0)),
      new Room("AMPHI 7", 50, 3, RoomType.AMPHI, campuses.get(0)),
      new Room("AMPHI 3", 50, 3, RoomType.AMPHI, campuses.get(1)),
      new Room("Salle Polyvalente", 120, 0, RoomType.POLYVALENTE, campuses.get(1)),
      new Room("Lab Cisco S1", 30, 1, RoomType.TP, campuses.get(2)),
      new Room("Lab Java S2", 30, 2, RoomType.TP, campuses.get(2)),
      new Room("Salle 104", 40, 1, RoomType.POLYVALENTE, campuses.get(0))
    );
    return roomRepository.saveAll(rooms);
  }

  private List<DayPlan> seedTimeTables(List<Element> elements, List<Room> rooms) {
    List<DayPlan> plans = List.of(
      new DayPlan(
        LocalDate.of(2026, 4, 6), LocalTime.of(9, 0), LocalTime.of(11, 0),
        1, PlanType.LECTURE, elements.get(0), rooms.get(1) // Analyse
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 6), LocalTime.of(14, 30), LocalTime.of(16, 30),
        1, PlanType.LECTURE, elements.get(2), rooms.get(0) // Algo
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 7), LocalTime.of(9, 0), LocalTime.of(11, 0),
        1, PlanType.LECTURE, elements.get(4), rooms.get(1) // Architecture
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 7), LocalTime.of(14, 0), LocalTime.of(16, 0),
        1, PlanType.LECTURE, elements.get(6), rooms.get(2) // Français
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 8), LocalTime.of(8, 30), LocalTime.of(11, 0),
        1, PlanType.LECTURE, elements.get(1), rooms.get(1) // Algèbre
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 8), LocalTime.of(16, 0), LocalTime.of(18, 0),
        1, PlanType.LECTURE, elements.get(3), rooms.get(0) // Prog C
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 9), LocalTime.of(10, 30), LocalTime.of(12, 30),
        1, PlanType.LECTURE, elements.get(5), rooms.get(4) // OS
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 9), LocalTime.of(15, 30), LocalTime.of(17, 30),
        1, PlanType.LECTURE, elements.get(7), rooms.get(2) // Anglais
      )
    );
    return dayPlanRepository.saveAll(plans);
  }

  private void seedResults(List<Element> elements, List<User> users) {
    List<Result> results = List.of(
      new Result(14.5, users.get(1), elements.get(0)),
      new Result(17.0, users.get(1), elements.get(2)),
      new Result(16.5, users.get(1), elements.get(4)),
      new Result(18.0, users.get(1), elements.get(6)),
      new Result(15.0, users.get(1), elements.get(7)),
      
      new Result(12.0, users.get(3), elements.get(0)),
      new Result(10.5, users.get(3), elements.get(2)),
      new Result(14.0, users.get(3), elements.get(6)),
      
      new Result(16.0, users.get(2), elements.get(8)),
      new Result(13.5, users.get(2), elements.get(10))
    );
    resultRepository.saveAll(results);
  }

  private List<Absence> seedAbsence(List<User> users, List<Element> elements) {
    List<Absence> absences = List.of(
      new Absence(LocalDateTime.now(), AbsenceType.CLASS, true, users.get(1), elements.get(0)),
      new Absence(LocalDateTime.now().minusDays(5), AbsenceType.CLASS, false, users.get(1), elements.get(2)),
      new Absence(LocalDateTime.now(), AbsenceType.CLASS, false, users.get(1), elements.get(4)),
      new Absence(LocalDateTime.now(), AbsenceType.EXAM, false, users.get(2), elements.get(8)),
      new Absence(LocalDateTime.now().minusDays(2), AbsenceType.CLASS, true, users.get(2), elements.get(10)),
      new Absence(LocalDateTime.now().minusDays(10), AbsenceType.CLASS, true, users.get(3), elements.get(6)),
      new Absence(LocalDateTime.now().minusDays(1), AbsenceType.CLASS, false, users.get(4), elements.get(16))
    );
    return absenceRepository.saveAll(absences);
  }

  private void seedRequests(List<User> users) {
    User student1 = users.get(1);
    User student2 = users.get(2);
    User student3 = users.get(3);

    List<Request> requests = List.of(
      new Request(null, "Demande de convention de stage", RequestState.PENDING, new Date(), null, student1),
      new Request(null, "Demande de relevé de notes S1", RequestState.IN_PROGRESS, addDays(new Date(), -2), null, student1),
      new Request(null, "Justification d'absence médicale", RequestState.COMPLETED, addDays(new Date(), -5), new Date(), student1),
      
      new Request(null, "Changement de groupe de TP", RequestState.PENDING, new Date(), null, student2),
      new Request(null, "Attestation de scolarité", RequestState.COMPLETED, addDays(new Date(), -10), addDays(new Date(), -8), student2),
      
      new Request(null, "Demande de relevé de notes S1", RequestState.PENDING, new Date(), null, student3)
    );
    requestRepository.saveAll(requests);
  }

  private Date addDays(Date date, int days) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_YEAR, days);
    return cal.getTime();
  }
}
