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
      new User("soufiane", "jaber", "soufiane", DepartmentName.GENIE_INFORMATIQUE,
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
      new Module("Programmation Resaux", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Programmation Python", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Programmation OOP", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Base De donnee", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Langues entrangères", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("CMS", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Génie Logiciel", 1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("UML", 1, DepartmentName.GENIE_INFORMATIQUE),
      
      new Module("Mysql", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Python Machine learning", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Java EE", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("ENGLISH 2", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("FRENCH 2", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("UML 2", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Project interdisciplinaire", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Base de donnee Oracle", 2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Inteligence Artificielle", 2, DepartmentName.GENIE_INFORMATIQUE),

      new Module("Backend Java Spring Boot", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Frontend React & Next.js", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Algorithmique avancée et C", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Systèmes d'Exploitation Debian", 3, DepartmentName.GENIE_INFORMATIQUE),
      new Module("DevOps & CI/CD", 3, DepartmentName.GENIE_INFORMATIQUE)
    );
    return moduleRepository.saveAll(modules);
  }

  private List<Element> seedElements(List<Module> modules, List<User> users) {
    List<Element> elements = List.of(
      new Element("TCP/IP", modules.get(0), users.get(6)),
      new Element("Programmation client serveur", modules.get(0), users.get(7)),
      new Element("Data Science", modules.get(1), users.get(6)),
      new Element("Python POO", modules.get(1), users.get(6)),
      new Element("Java POO", modules.get(2), users.get(8)),
      new Element("UML", modules.get(2), users.get(8)),
      new Element("MSSQL Server", modules.get(3), users.get(9)),
      new Element("ENGLISH 1", modules.get(4), users.get(10)),
      new Element("FRENCH 1", modules.get(4), users.get(10)),
      new Element("CMS", modules.get(5), users.get(11)),
      new Element("Génie Logiciel", modules.get(6), users.get(11)),
      new Element("Requêtes SQL complexes", modules.get(8), users.get(9)),
      new Element("Réseaux de Neurones", modules.get(9), users.get(6)),
      new Element("Architecture MVC Java", modules.get(10), users.get(8)),
      new Element("Communication pro", modules.get(11), users.get(10)),
      new Element("Diagrammes de classe", modules.get(13), users.get(8)),
      new Element("Spring Core & Security", modules.get(17), users.get(7)),
      new Element("Microservices Spring Cloud", modules.get(17), users.get(7)),
      new Element("React Hooks & State", modules.get(18), users.get(11)),
      new Element("Next.js App Router", modules.get(18), users.get(11)),
      new Element("Pointeurs et GDB Debugging", modules.get(19), users.get(6)),
      new Element("Linux i3 Config & Scripting", modules.get(20), users.get(9))
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
      new Room("Lab Linux S1", 30, 1, RoomType.TP, campuses.get(2)),
      new Room("Lab Java S2", 30, 2, RoomType.TP, campuses.get(2)),
      new Room("Salle 104", 40, 1, RoomType.POLYVALENTE, campuses.get(0))
    );
    return roomRepository.saveAll(rooms);
  }

private List<DayPlan> seedTimeTables(List<Element> elements, List<Room> rooms) {
    List<DayPlan> plans = List.of(
      new DayPlan(
        LocalDate.of(2026, 4, 6), LocalTime.of(9, 0), LocalTime.of(11, 0),
        1, PlanType.LECTURE, elements.get(0), rooms.get(3)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 6), LocalTime.of(14, 30), LocalTime.of(16, 30),
        1, PlanType.LECTURE, elements.get(1), rooms.get(0)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 7), LocalTime.of(9, 0), LocalTime.of(11, 0),
        1, PlanType.LECTURE, elements.get(2), rooms.get(0)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 7), LocalTime.of(14, 0), LocalTime.of(16, 0),
        1, PlanType.LECTURE, elements.get(3), rooms.get(1)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 8), LocalTime.of(8, 30), LocalTime.of(11, 0),
        1, PlanType.LECTURE, elements.get(4), rooms.get(1)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 8), LocalTime.of(16, 0), LocalTime.of(18, 0),
        1, PlanType.LECTURE, elements.get(5), rooms.get(1)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 9), LocalTime.of(10, 30), LocalTime.of(12, 30),
        1, PlanType.LECTURE, elements.get(6), rooms.get(1)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 9), LocalTime.of(15, 30), LocalTime.of(17, 30),
        1, PlanType.LECTURE, elements.get(7), rooms.get(2)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 10), LocalTime.of(8, 30), LocalTime.of(10, 30),
        1, PlanType.LECTURE, elements.get(8), rooms.get(2)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 10), LocalTime.of(15, 0), LocalTime.of(17, 0),
        1, PlanType.LECTURE, elements.get(9), rooms.get(0)
      ),
      new DayPlan(
        LocalDate.of(2026, 4, 11), LocalTime.of(8, 30), LocalTime.of(10, 30),
        1, PlanType.LECTURE, elements.get(10), rooms.get(1)
      ),
      new DayPlan(
        LocalDate.of(2026, 7, 15), LocalTime.of(9, 0), LocalTime.of(12, 0),
        3, PlanType.LECTURE, elements.get(16), rooms.get(5)
      ),
      new DayPlan(
        LocalDate.of(2026, 7, 15), LocalTime.of(14, 0), LocalTime.of(17, 0),
        3, PlanType.LECTURE, elements.get(18), rooms.get(5)
      ),
      new DayPlan(
        LocalDate.of(2026, 7, 16), LocalTime.of(9, 0), LocalTime.of(11, 0),
        3, PlanType.LECTURE, elements.get(20), rooms.get(4)
      ),
      new DayPlan(
        LocalDate.of(2026, 7, 17), LocalTime.of(14, 0), LocalTime.of(16, 0),
        3, PlanType.LECTURE, elements.get(21), rooms.get(4)
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
      new Absence(LocalDateTime.now(), AbsenceType.CLASS, true, users.get(1), elements.get(0)),
      new Absence(LocalDateTime.now(), AbsenceType.EXAM, false, users.get(1), elements.get(3)),
      new Absence(LocalDateTime.now(), AbsenceType.CLASS, false, users.get(1), elements.get(6)),
      new Absence(LocalDateTime.now().minusDays(5), AbsenceType.CLASS, false, users.get(1), elements.get(6)),
      new Absence(LocalDateTime.now(), AbsenceType.EXAM, false, users.get(2), elements.get(1)),
      new Absence(LocalDateTime.now().minusDays(2), AbsenceType.CLASS, true, users.get(2), elements.get(1)),
      new Absence(LocalDateTime.now(), AbsenceType.EXAM, false, users.get(2), elements.get(6)),
      new Absence(LocalDateTime.now().minusDays(10), AbsenceType.CLASS, true, users.get(3), elements.get(4)),
      new Absence(LocalDateTime.now().minusDays(1), AbsenceType.CLASS, false, users.get(4), elements.get(16))
    );
    return absenceRepository.saveAll(absences);
  }

  private void seedRequests(List<User> users) {
    User student1 = users.get(1);
    User student2 = users.get(2);
    User student3 = users.get(3);

    List<Request> requests = List.of(
      new Request(null, "Document Request A", RequestState.PENDING, new Date(), null, student1),
      new Request(null, "Transcript Request", RequestState.IN_PROGRESS, addDays(new Date(), -2), null, student1),
      new Request(null, "Absence Justification", RequestState.COMPLETED, addDays(new Date(), -5), new Date(), student1),
      new Request(null, "Library Access", RequestState.PENDING, new Date(), null, student1),
      new Request(null, "Course Change", RequestState.IN_PROGRESS, addDays(new Date(), -1), null, student1),
      new Request(null, "Internship Convention (Java Backend)", RequestState.PENDING, new Date(), null, student1),
      new Request(null, "Exam Retake", RequestState.PENDING, new Date(), null, student2),
      new Request(null, "Medical Certificate",
        RequestState.COMPLETED, addDays(new Date(), -10), addDays(new Date(), -8), student2),
      new Request(null, "Hardware Loan", RequestState.IN_PROGRESS, addDays(new Date(), -3), null, student2),
      new Request(null, "Study Abroad Inquiry", RequestState.PENDING, new Date(), null, student2),
      new Request(
        null, "Software License Request", RequestState.COMPLETED,
        addDays(new Date(), -7), addDays(new Date(), -6), student2),
      new Request(null, "Relevé de notes S1", RequestState.PENDING, new Date(), null, student3)
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
