package com.elearn.api.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.elearn.api.entity.Absence;
import com.elearn.api.entity.AbsenceType;
import com.elearn.api.entity.Campus;
import com.elearn.api.entity.DayPlan;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Module;
import com.elearn.api.entity.PlanType;
import com.elearn.api.entity.Request;
import com.elearn.api.entity.RequestState;
import com.elearn.api.entity.Result;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.Room;
import com.elearn.api.entity.RoomType;
import com.elearn.api.entity.StudyMode;
import com.elearn.api.entity.User;
import com.elearn.api.repository.AbsenceRepository;
import com.elearn.api.repository.CampusRepository;
import com.elearn.api.repository.DayPlanRepository;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.ModuleRepository;
import com.elearn.api.repository.RequestRepository;
import com.elearn.api.repository.ResultRepository;
import com.elearn.api.repository.RoomRepository;
import com.elearn.api.repository.UserRepository;
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
        return;
      }

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
    }
  }

  private List<User> seedUsers() {
    List<User> users = List.of(
      new User(
        "Admin", "System", "admin", null, "admin@univ-tech.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1990, 5, 15),
        Role.ADMIN, null, 0, 0, null
      ),
      new User(
        "Soufiane", "Jaber", "soufiane", DepartmentName.GENIE_INFORMATIQUE,
        "soufianejb@mail.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2005, 11, 12), Role.STUDENT, StudyMode.HYBRID, 1, 1, "A"
      ),
      new User(
        "Sara", "Alaoui", "sara-a", DepartmentName.GENIE_CIVIL,
        "sara.alaoui@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2002, 10, 10), Role.STUDENT, StudyMode.ON_SITE, 2, 1, "A"
      ),
      new User(
        "Omar", "Fassi", "omar-f", DepartmentName.GENIE_INFORMATIQUE,
        "omar.fassi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2004, 3, 22), Role.STUDENT, StudyMode.ON_SITE, 1, 1, "B"
      ),
      new User(
        "Lina", "Idrissi", "lina-i", DepartmentName.GENIE_INFORMATIQUE,
        "lina.idrissi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2003, 7, 8), Role.STUDENT, StudyMode.HYBRID, 2, 1, "A"
      ),
      new User(
        "Mehdi", "Bennani", "mehdi-b", DepartmentName.GENIE_INFORMATIQUE,
        "mehdi.bennani@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2004, 12, 1), Role.STUDENT, StudyMode.ON_SITE, 1, 2, "A"
      ),
      new User(
        "Driss", "Mansouri", "dmansouri", DepartmentName.GENIE_INFORMATIQUE,
        "driss.m@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1985, 1, 5), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Hassan", "Zaki", "hzaki", DepartmentName.GENIE_INFORMATIQUE,
        "h.zaki@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1985, 1, 5), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Nadia", "Amrani", "namrani", DepartmentName.GENIE_INFORMATIQUE,
        "n.amrani@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1978, 9, 14), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Youssef", "Kabbaj", "ykabbaj", DepartmentName.GENIE_INFORMATIQUE,
        "y.kabbaj@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1982, 4, 30), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Fatima", "Zahra", "fzahra", DepartmentName.GENIE_INFORMATIQUE,
        "f.zahra@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1975, 11, 22), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Karim", "Tazi", "ktazi", DepartmentName.GENIE_INFORMATIQUE,
        "k.tazi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1988, 2, 18), Role.TEACHER, null, 0, 0, null
      )
    );
    return userRepository.saveAll(users);
  }

  private List<Module> seedModules() {
    DepartmentName gi = DepartmentName.GENIE_INFORMATIQUE;
    List<Module> modules = List.of(
      new Module("Reseaux Informatiques", 1, 1, gi),
      new Module("Programmation Python", 1, 1, gi),
      new Module("Algorithmique Avancee", 1, 1, gi),
      new Module("Bases de Donnees", 1, 1, gi),
      new Module("Langues Etrangeres", 1, 1, gi),
      new Module("Systemes d'Information", 1, 2, gi),
      new Module("Genie Logiciel", 1, 2, gi),
      new Module("Modelisation UML", 1, 2, gi),
      new Module("Administration SGBD", 2, 1, gi),
      new Module("Machine Learning", 2, 1, gi),
      new Module("Developpement Java EE", 2, 1, gi),
      new Module("Communication Avancee", 2, 1, gi),
      new Module("Francais Technique", 2, 2, gi),
      new Module("Conception Logicielle", 2, 2, gi),
      new Module("Projet Interdisciplinaire", 2, 2, gi),
      new Module("Bases de Donnees Oracle", 2, 2, gi),
      new Module("Intelligence Artificielle", 2, 2, gi),
      new Module("Developpement Backend", 3, 1, gi),
      new Module("Developpement Frontend", 3, 1, gi),
      new Module("Structure de Donnees", 3, 1, gi),
      new Module("Systemes d'Exploitation", 3, 2, gi),
      new Module("DevOps & CI/CD", 3, 2, gi)
    );
    return moduleRepository.saveAll(modules);
  }

  private List<Element> seedElements(List<Module> modules, List<User> users) {
    List<Element> elements = List.of(
      new Element("Modele OSI et TCP/IP", modules.get(0), users.get(6)),
      new Element("Programmation Socket", modules.get(0), users.get(7)),
      new Element("Analyse de Donnees", modules.get(1), users.get(6)),
      new Element("Structures Python", modules.get(1), users.get(6)),
      new Element("Concepts Avances", modules.get(2), users.get(8)),
      new Element("Arbres et Graphes", modules.get(2), users.get(8)),
      new Element("SQL Avance", modules.get(3), users.get(9)),
      new Element("Anglais Technique", modules.get(4), users.get(10)),
      new Element("Expression Ecrite", modules.get(4), users.get(10)),
      new Element("Gestion de Projet", modules.get(5), users.get(11)),
      new Element("Methodes Agiles", modules.get(6), users.get(11)),
      new Element("Optimisation SQL", modules.get(8), users.get(9)),
      new Element("Reseaux de Neurones", modules.get(9), users.get(6)),
      new Element("Architecture Java", modules.get(10), users.get(8)),
      new Element("Prise de Parole", modules.get(11), users.get(10)),
      new Element("Design Patterns", modules.get(13), users.get(8)),
      new Element("Securite Applicative", modules.get(17), users.get(7)),
      new Element("Microservices", modules.get(17), users.get(7)),
      new Element("Interfaces Reactives", modules.get(18), users.get(11)),
      new Element("Gestion d'Etat", modules.get(18), users.get(11)),
      new Element("Complexite Avancee", modules.get(19), users.get(6)),
      new Element("Administration Serveur", modules.get(20), users.get(9))
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
      new Room(
        "Salle Polyvalente", 120, 0, RoomType.POLYVALENTE, campuses.get(1)
      ),
      new Room("Lab Reseaux S1", 30, 1, RoomType.TP, campuses.get(2)),
      new Room("Lab Java S2", 30, 2, RoomType.TP, campuses.get(2)),
      new Room("Salle 104", 40, 1, RoomType.POLYVALENTE, campuses.get(0))
    );
    return roomRepository.saveAll(rooms);
  }

  private List<DayPlan> seedTimeTables(
    List<Element> elements,
    List<Room> rooms
  ) {
    LocalDate today = LocalDate.now();
    LocalDate base = today.with(ChronoField.DAY_OF_WEEK, 1);

    if (today.getMonthValue() == 7 || today.getMonthValue() == 8) {
      base = LocalDate.of(today.getYear(), 9, 15)
        .with(ChronoField.DAY_OF_WEEK, 1);
    }

    DepartmentName gi = DepartmentName.GENIE_INFORMATIQUE;

    List<DayPlan> plans = List.of(
      new DayPlan(
        base, LocalTime.of(9, 0), LocalTime.of(11, 0),
        15, PlanType.LECTURE, gi, 1, 1, "A", elements.get(0), rooms.get(3)
      ),
      new DayPlan(
        base, LocalTime.of(14, 30), LocalTime.of(16, 30),
        15, PlanType.LECTURE, gi, 1, 1, "A", elements.get(1), rooms.get(0)
      ),
      new DayPlan(
        base.plusDays(1), LocalTime.of(9, 0), LocalTime.of(11, 0),
        15, PlanType.LECTURE, gi, 1, 1, "A", elements.get(2), rooms.get(0)
      ),
      new DayPlan(
        base.plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0),
        15, PlanType.LECTURE, gi, 1, 1, "B", elements.get(3), rooms.get(1)
      ),
      new DayPlan(
        base.plusDays(2), LocalTime.of(8, 30), LocalTime.of(11, 0),
        15, PlanType.LECTURE, gi, 1, 1, "A", elements.get(4), rooms.get(1)
      ),
      new DayPlan(
        base.plusDays(2), LocalTime.of(16, 0), LocalTime.of(18, 0),
        15, PlanType.LECTURE, gi, 1, 1, "B", elements.get(5), rooms.get(1)
      ),
      new DayPlan(
        base.plusDays(3), LocalTime.of(10, 30), LocalTime.of(12, 30),
        15, PlanType.LECTURE, gi, 1, 1, "A", elements.get(6), rooms.get(1)
      ),
      new DayPlan(
        base.plusDays(3), LocalTime.of(15, 30), LocalTime.of(17, 30),
        15, PlanType.LECTURE, gi, 1, 1, "A", elements.get(7), rooms.get(2)
      ),
      new DayPlan(
        base.plusDays(4), LocalTime.of(8, 30), LocalTime.of(10, 30),
        15, PlanType.LECTURE, gi, 1, 2, "A", elements.get(9), rooms.get(2)
      ),
      new DayPlan(
        base.plusDays(4), LocalTime.of(15, 0), LocalTime.of(17, 0),
        15, PlanType.LECTURE, gi, 2, 1, "A", elements.get(11), rooms.get(0)
      ),
      new DayPlan(
        base.plusDays(5), LocalTime.of(8, 30), LocalTime.of(10, 30),
        15, PlanType.LECTURE, gi, 2, 1, "A", elements.get(13), rooms.get(1)
      )
    );
    return dayPlanRepository.saveAll(plans);
  }

  private void seedResults(List<Element> elements, List<User> users) {
    List<Result> results = List.of(
      new Result(14, users.get(1), elements.get(0)),
      new Result(17, users.get(1), elements.get(1)),
      new Result(16, users.get(1), elements.get(2)),
      new Result(18, users.get(1), elements.get(4)),
      new Result(15, users.get(1), elements.get(6)),
      new Result(12, users.get(3), elements.get(0)),
      new Result(10, users.get(3), elements.get(1)),
      new Result(14, users.get(3), elements.get(4)),
      new Result(16, users.get(2), elements.get(12)),
      new Result(13, users.get(2), elements.get(13))
    );
    resultRepository.saveAll(results);
  }

  private List<Absence> seedAbsence(List<User> users, List<Element> elements) {
    List<Absence> absences = List.of(
      new Absence(
        LocalDateTime.now(), AbsenceType.CLASS, true,
        users.get(1), elements.get(0)
      ),
      new Absence(
        LocalDateTime.now(), AbsenceType.EXAM, false,
        users.get(1), elements.get(3)
      ),
      new Absence(
        LocalDateTime.now(), AbsenceType.CLASS, false,
        users.get(1), elements.get(6)
      ),
      new Absence(
        LocalDateTime.now().minusDays(5), AbsenceType.CLASS, false,
        users.get(1), elements.get(6)
      ),
      new Absence(
        LocalDateTime.now(), AbsenceType.EXAM, false,
        users.get(2), elements.get(1)
      ),
      new Absence(
        LocalDateTime.now().minusDays(2), AbsenceType.CLASS, true,
        users.get(2), elements.get(1)
      ),
      new Absence(
        LocalDateTime.now(), AbsenceType.EXAM, false,
        users.get(2), elements.get(6)
      ),
      new Absence(
        LocalDateTime.now().minusDays(10), AbsenceType.CLASS, true,
        users.get(3), elements.get(4)
      ),
      new Absence(
        LocalDateTime.now().minusDays(1), AbsenceType.CLASS, false,
        users.get(4), elements.get(16)
      )
    );
    return absenceRepository.saveAll(absences);
  }

  private void seedRequests(List<User> users) {
    User student1 = users.get(1);
    User student2 = users.get(2);
    User student3 = users.get(3);
    List<Request> requests = List.of(
      new Request(
        null, "Document Request A", RequestState.PENDING,
        new Date(), null, student1
      ),
      new Request(
        null, "Transcript Request", RequestState.IN_PROGRESS,
        addDays(new Date(), -2), null, student1
      ),
      new Request(
        null, "Absence Justification", RequestState.COMPLETED,
        addDays(new Date(), -5), new Date(), student1
      ),
      new Request(
        null, "Library Access", RequestState.PENDING,
        new Date(), null, student1
      ),
      new Request(
        null, "Course Change", RequestState.IN_PROGRESS,
        addDays(new Date(), -1), null, student1
      ),
      new Request(
        null, "Internship Convention", RequestState.PENDING,
        new Date(), null, student1
      ),
      new Request(
        null, "Exam Retake", RequestState.PENDING,
        new Date(), null, student2
      ),
      new Request(
        null, "Medical Certificate", RequestState.COMPLETED,
        addDays(new Date(), -10), addDays(new Date(), -8), student2
      ),
      new Request(
        null, "Hardware Loan", RequestState.IN_PROGRESS,
        addDays(new Date(), -3), null, student2
      ),
      new Request(
        null, "Study Abroad Inquiry", RequestState.PENDING,
        new Date(), null, student2
      ),
      new Request(
        null, "Software License Request", RequestState.COMPLETED,
        addDays(new Date(), -7), addDays(new Date(), -6), student2
      ),
      new Request(
        null, "Releve de notes S1", RequestState.PENDING,
        new Date(), null, student3
      )
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
