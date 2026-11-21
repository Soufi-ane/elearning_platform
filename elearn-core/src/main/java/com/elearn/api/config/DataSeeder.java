package com.elearn.api.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
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
import com.elearn.api.repository.AssignmentSubmissionRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.elearn.api.entity.FileType;
import com.elearn.api.entity.Material;
import com.elearn.api.entity.MaterialFolder;
import com.elearn.api.repository.MaterialFolderRepository;
import com.elearn.api.repository.MaterialRepository;
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
  private final MaterialFolderRepository materialFolderRepository;
  private final MaterialRepository materialRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;

  @Value("${ENVIRONMENT}")
  private String environment;

  @Override
  public void run(String... args) throws Exception {
    boolean forceSeed = args.length > 0 && "seed".equalsIgnoreCase(args[0]);
    boolean emptyDb =
      "DEV".equalsIgnoreCase(environment) &&
      (moduleRepository.count() == 0 ||
       elementRepository.count() == 0 ||
       dayPlanRepository.count() < 30);

    if (forceSeed || emptyDb) {
      if (!"DEV".equalsIgnoreCase(environment) && userRepository.count() > 0) {
        return;
      }

      dayPlanRepository.deleteAllInBatch();
      absenceRepository.deleteAllInBatch();
      resultRepository.deleteAllInBatch();
      assignmentSubmissionRepository.deleteAllInBatch();
      materialRepository.deleteAllInBatch();
      materialFolderRepository.deleteAllInBatch();
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
      seedGrades();
      seedMaterials(elements);
    }
    ensureRootAdmin();
  }

  private void seedGrades() {
    if (resultRepository.count() > 0) {
      return;
    }

    List<User> students = userRepository.findByRole(Role.STUDENT);
    List<User> admins = userRepository.findByRole(Role.ADMIN);
    List<Element> elements = elementRepository.findAll();

    if (students.isEmpty() || elements.isEmpty()) {
      return;
    }

    User primaryAdmin = admins.isEmpty() ? null : admins.get(0);
    java.util.Random random = new java.util.Random(42L);
    List<Result> batch = new java.util.ArrayList<>();
    int counter = 0;

    for (User student : students) {
      int completedUpTo = student.getSemester() - 1;
      if (completedUpTo < 1) {
        continue;
      }

      double baseline = 9.5 + (random.nextDouble() * 7.5);

      for (Element element : elements) {
        if (element.getModule() == null) {
          continue;
        }
        if (element.getModule().getDepartment() != student.getDepartment()) {
          continue;
        }
        if (element.getModule().getSemester() > completedUpTo) {
          continue;
        }

        double jitter = (random.nextDouble() * 5.0) - 2.5;
        double raw = Math.max(3.5, Math.min(19.75, baseline + jitter));
        double grade = Math.round(raw * 4.0) / 4.0;
        Result result = new Result(
          grade,
          student,
          element,
          element.getTeacher(),
          pickGradeRemark(grade)
        );

        if (primaryAdmin != null && counter % 11 == 0 && grade < 18.5) {
          result.setGrade(Math.min(20.0, grade + 0.5));
          result.setLastModifiedBy(primaryAdmin);
          result.setRemarks("Re-evaluation approved");
        }

        batch.add(result);
        counter++;
      }
    }

    resultRepository.saveAll(batch);
  }

  private String pickGradeRemark(double grade) {
    if (grade >= 16.0) {
      return "Excellent mastery";
    }
    if (grade >= 14.0) {
      return "Strong performance";
    }
    if (grade >= 10.0) {
      return "Validated";
    }
    if (grade >= 7.0) {
      return "Retake session required";
    }
    return "Insufficient score";
  }

  private List<User> seedUsers() {
    DepartmentName gi = DepartmentName.GENIE_INFORMATIQUE;
    DepartmentName ge = DepartmentName.GENIE_ELECTRIQUE;
    DepartmentName gc = DepartmentName.GENIE_CIVIL;
    User root = new User(
      "Admin", "System", "admin", null, "admin@univ-tech.ma",
      passwordEncoder.encode("12341234"), LocalDate.of(1990, 5, 15),
      Role.ADMIN, null, 0, 0, null
    );
    root.setRootAdmin(true);
    List<User> users = List.of(
      root,
      new User(
        "Soufiane", "Jaber", "soufiane", gi,
        "soufianejb@mail.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2005, 11, 12), Role.STUDENT, StudyMode.HYBRID, 1, 1, "A"
      ),
      new User(
        "Omar", "Fassi", "omar-f", gi,
        "omar.fassi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2004, 3, 22), Role.STUDENT, StudyMode.ON_SITE, 1, 1, "B"
      ),
      new User(
        "Mehdi", "Bennani", "mehdi-b", gi,
        "mehdi.bennani@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2004, 12, 1), Role.STUDENT, StudyMode.ON_SITE, 1, 2, "A"
      ),
      new User(
        "Lina", "Idrissi", "lina-i", gi,
        "lina.idrissi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2003, 7, 8), Role.STUDENT, StudyMode.HYBRID, 2, 1, "A"
      ),
      new User(
        "Amine", "Chraibi", "amine-c", gi,
        "amine.chraibi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2003, 2, 19), Role.STUDENT, StudyMode.ON_SITE, 2, 2, "B"
      ),
      new User(
        "Yasmine", "Berrada", "yasmine-b", gi,
        "yasmine.berrada@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2002, 6, 14), Role.STUDENT, StudyMode.HYBRID, 3, 1, "A"
      ),
      new User(
        "Tariq", "Tahiri", "tariq-t", ge,
        "tariq.tahiri@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2005, 4, 11), Role.STUDENT, StudyMode.ON_SITE, 1, 1, "A"
      ),
      new User(
        "Salma", "Kettani", "salma-k", ge,
        "salma.kettani@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2004, 8, 25), Role.STUDENT, StudyMode.HYBRID, 2, 1, "B"
      ),
      new User(
        "Hamza", "Ouazzani", "hamza-o", ge,
        "hamza.ouazzani@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2002, 11, 3), Role.STUDENT, StudyMode.ON_SITE, 3, 2, "A"
      ),
      new User(
        "Sara", "Alaoui", "sara-a", gc,
        "sara.alaoui@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2002, 10, 10), Role.STUDENT, StudyMode.ON_SITE, 1, 1, "A"
      ),
      new User(
        "Zineb", "Lahlou", "zineb-l", gc,
        "zineb.lahlou@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2003, 9, 17), Role.STUDENT, StudyMode.ON_SITE, 2, 1, "B"
      ),
      new User(
        "Rayan", "Filali", "rayan-f", gc,
        "rayan.filali@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(2002, 1, 29), Role.STUDENT, StudyMode.HYBRID, 3, 1, "A"
      ),
      new User(
        "Driss", "Mansouri", "dmansouri", gi,
        "driss.m@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1985, 1, 5), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Hassan", "Zaki", "hzaki", gi,
        "h.zaki@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1985, 1, 5), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Nadia", "Amrani", "namrani", gi,
        "n.amrani@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1978, 9, 14), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Youssef", "Kabbaj", "ykabbaj", ge,
        "y.kabbaj@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1982, 4, 30), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Fatima", "Zahra", "fzahra", ge,
        "f.zahra@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1975, 11, 22), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Karim", "Tazi", "ktazi", gc,
        "k.tazi@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1988, 2, 18), Role.TEACHER, null, 0, 0, null
      ),
      new User(
        "Meryem", "Bennis", "mbennis", gc,
        "m.bennis@univ-tech.ma", passwordEncoder.encode("12341234"),
        LocalDate.of(1984, 7, 12), Role.TEACHER, null, 0, 0, null
      )
    );
    return userRepository.saveAll(users);
  }

  private List<Module> seedModules() {
    DepartmentName gi = DepartmentName.GENIE_INFORMATIQUE;
    DepartmentName ge = DepartmentName.GENIE_ELECTRIQUE;
    DepartmentName gc = DepartmentName.GENIE_CIVIL;

    List<Module> modules = List.of(
      new Module("Reseaux Informatiques", 1, 1, gi),
      new Module("Programmation Python & C", 1, 1, gi),
      new Module("Algorithmique & Structures", 1, 1, gi),
      new Module("Bases de Donnees Relationnelles", 1, 1, gi),
      new Module("Systemes d'Information", 1, 2, gi),
      new Module("Genie Logiciel & UML", 1, 2, gi),
      new Module("Architecture des Ordinateurs", 1, 2, gi),
      new Module("Developpement Web", 1, 2, gi),
      new Module("Administration SGBD", 2, 1, gi),
      new Module("Developpement Java EE", 2, 1, gi),
      new Module("Machine Learning", 2, 1, gi),
      new Module("Systemes d'Exploitation Linux", 2, 1, gi),
      new Module("Conception Logicielle & Patterns", 2, 2, gi),
      new Module("Bases de Donnees Oracle", 2, 2, gi),
      new Module("Intelligence Artificielle", 2, 2, gi),
      new Module("Securite des Reseaux", 2, 2, gi),
      new Module("Architecture Microservices", 3, 1, gi),
      new Module("Developpement Frontend Angular", 3, 1, gi),
      new Module("Cloud Computing & Virtualisation", 3, 1, gi),
      new Module("DevOps & CI/CD", 3, 2, gi),
      new Module("Big Data & Systemes Distribues", 3, 2, gi),
      new Module("Audit & Securite Applicative", 3, 2, gi),

      new Module("Circuits Electriques", 1, 1, ge),
      new Module("Electronique Analogique", 1, 1, ge),
      new Module("Systemes Logiques & VHDL", 1, 2, ge),
      new Module("Electromagnetisme", 1, 2, ge),
      new Module("Automatique Lineaire", 2, 1, ge),
      new Module("Machines Electriques", 2, 1, ge),
      new Module("Electronique de Puissance", 2, 2, ge),
      new Module("Traitement du Signal", 2, 2, ge),
      new Module("Systemes Embarques & IoT", 3, 1, ge),
      new Module("Robotique Industrielle", 3, 1, ge),
      new Module("Reseaux Electriques Intelligents", 3, 2, ge),
      new Module("Energies Renouvelables", 3, 2, ge),

      new Module("Mecanique des Solides", 1, 1, gc),
      new Module("Materiaux de Construction", 1, 1, gc),
      new Module("Topographie & Cartographie", 1, 2, gc),
      new Module("Resistance des Materiaux", 1, 2, gc),
      new Module("Beton Arme", 2, 1, gc),
      new Module("Hydraulique Generale", 2, 1, gc),
      new Module("Geotechnique & Fondations", 2, 2, gc),
      new Module("Construction Metallique", 2, 2, gc),
      new Module("Calcul des Structures & Elements Finis", 3, 1, gc),
      new Module("Ponts & Ouvrages d'Art", 3, 1, gc),
      new Module("Genie Parasismique", 3, 2, gc),
      new Module("Gestion de Chantier & BIM", 3, 2, gc)
    );
    return moduleRepository.saveAll(modules);
  }

  private List<Element> seedElements(List<Module> modules, List<User> users) {
    List<User> teachers = users.stream()
      .filter(u -> u.getRole() == Role.TEACHER)
      .toList();
    List<Element> elements = new ArrayList<>();

    for (int i = 0; i < modules.size(); i++) {
      Module m = modules.get(i);
      User t1 = teachers.get(i % teachers.size());
      User t2 = teachers.get((i + 1) % teachers.size());
      elements.add(new Element(m.getName() + " - Cours", m, t1));
      elements.add(new Element(m.getName() + " - TD/TP", m, t2));
    }
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
      new Room("TP1", 35, 1, RoomType.TP, campuses.get(0)),
      new Room("TP2", 35, 3, RoomType.TP, campuses.get(0)),
      new Room("TP3", 35, 2, RoomType.TP, campuses.get(0)),
      new Room("AMPHI 1", 80, 0, RoomType.AMPHI, campuses.get(0)),
      new Room("AMPHI 7", 50, 3, RoomType.AMPHI, campuses.get(0)),
      new Room("Salle 104", 40, 1, RoomType.POLYVALENTE, campuses.get(0)),
      new Room("Salle 105", 40, 1, RoomType.POLYVALENTE, campuses.get(0)),
      new Room("AMPHI 3", 50, 3, RoomType.AMPHI, campuses.get(1)),
      new Room("AMPHI 4", 60, 2, RoomType.AMPHI, campuses.get(1)),
      new Room(
        "Salle Polyvalente", 120, 0, RoomType.POLYVALENTE, campuses.get(1)
      ),
      new Room("Salle 201", 35, 2, RoomType.POLYVALENTE, campuses.get(1)),
      new Room("Salle 202", 35, 2, RoomType.POLYVALENTE, campuses.get(1)),
      new Room("Lab Elec E1", 30, 1, RoomType.TP, campuses.get(1)),
      new Room("Lab Civil C1", 30, 0, RoomType.TP, campuses.get(1)),
      new Room("Lab Reseaux S1", 30, 1, RoomType.TP, campuses.get(2)),
      new Room("Lab Java S2", 30, 2, RoomType.TP, campuses.get(2)),
      new Room("Lab Cloud S3", 30, 2, RoomType.TP, campuses.get(2)),
      new Room("Salle 301", 40, 3, RoomType.POLYVALENTE, campuses.get(2)),
      new Room("Salle 302", 40, 3, RoomType.POLYVALENTE, campuses.get(2)),
      new Room("AMPHI Tech", 90, 0, RoomType.AMPHI, campuses.get(2))
    );
    return roomRepository.saveAll(rooms);
  }

  private List<DayPlan> seedTimeTables(
    List<Element> elements,
    List<Room> rooms
  ) {
    LocalDate today = LocalDate.now();
    int acadStartYear = (today.getMonthValue() >= 9)
      ? today.getYear()
      : today.getYear() - 1;

    LocalDate sem1Base = LocalDate.of(acadStartYear, 9, 15)
      .with(ChronoField.DAY_OF_WEEK, 1);
    LocalDate sem2Base = LocalDate.of(acadStartYear + 1, 2, 1)
      .with(ChronoField.DAY_OF_WEEK, 1);
    int semesterWeeks = 18;

    LocalTime[][] slots = new LocalTime[][] {
      { LocalTime.of(8, 30), LocalTime.of(10, 30) },
      { LocalTime.of(10, 45), LocalTime.of(12, 45) },
      { LocalTime.of(14, 0), LocalTime.of(16, 0) },
      { LocalTime.of(16, 15), LocalTime.of(18, 15) }
    };

    List<DayPlan> plans = new ArrayList<>();
    DepartmentName[] depts = DepartmentName.values();
    String[] groups = new String[] { "A", "B" };

    int roomCursor = 0;

    for (int dIdx = 0; dIdx < depts.length; dIdx++) {
      DepartmentName dept = depts[dIdx];
      for (int year = 1; year <= 3; year++) {
        for (int sem = 1; sem <= 2; sem++) {
          final int y = year;
          final int s = sem;
          List<Element> matchingEls = elements.stream()
            .filter(
              e ->
                e.getModule().getDepartment() == dept &&
                e.getModule().getYear() == y &&
                e.getModule().getSemester() == s
            )
            .toList();

          if (matchingEls.isEmpty()) {
            continue;
          }

          LocalDate startBase = (sem == 1) ? sem1Base : sem2Base;

          for (int gIdx = 0; gIdx < groups.length; gIdx++) {
            String grp = groups[gIdx];
            for (int dayOffset = 0; dayOffset < 5; dayOffset++) {
              Element el = matchingEls.get(dayOffset % matchingEls.size());
              int slotIdx =
                (dIdx + year + sem + gIdx + dayOffset) % slots.length;

              LocalDate sessionDate = startBase.plusDays(dayOffset);
              LocalTime startsAt = slots[slotIdx][0];
              LocalTime endsAt = slots[slotIdx][1];
              Room room = rooms.get(roomCursor % rooms.size());
              roomCursor++;

              PlanType planType =
                (dayOffset == 4) ? PlanType.EXAM : PlanType.LECTURE;

              plans.add(
                new DayPlan(
                  sessionDate,
                  startsAt,
                  endsAt,
                  semesterWeeks,
                  planType,
                  dept,
                  year,
                  sem,
                  grp,
                  el,
                  room
                )
              );
            }
          }
        }
      }
    }

    return dayPlanRepository.saveAll(plans);
  }

  private void seedResults(List<Element> elements, List<User> users) {
    List<Result> results = List.of(
      new Result(14, users.get(1), elements.get(0)),
      new Result(17, users.get(1), elements.get(1)),
      new Result(16, users.get(1), elements.get(2)),
      new Result(18, users.get(1), elements.get(4)),
      new Result(15, users.get(1), elements.get(6)),
      new Result(12, users.get(2), elements.get(0)),
      new Result(10, users.get(2), elements.get(1)),
      new Result(14, users.get(2), elements.get(4)),
      new Result(16, users.get(4), elements.get(16)),
      new Result(13, users.get(4), elements.get(18))
    );
    resultRepository.saveAll(results);
  }

  private List<Absence> seedAbsence(List<User> users, List<Element> elements) {
    List<Absence> absences = new ArrayList<>();
    List<User> students = users.stream()
      .filter(u -> u.getRole() == Role.STUDENT)
      .toList();

    int offset = 1;
    for (User student : students) {
      for (int sem = 1; sem <= 2; sem++) {
        final int targetSem = sem;
        List<Element> matchingEls = elements.stream()
          .filter(
            e ->
              e.getModule() != null &&
              e.getModule().getDepartment() == student.getDepartment() &&
              e.getModule().getYear() == student.getYear() &&
              e.getModule().getSemester() == targetSem
          )
          .toList();

        if (!matchingEls.isEmpty()) {
          Element el1 = matchingEls.get(0);
          absences.add(
            new Absence(
              LocalDateTime.now()
                .minusDays(offset)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0),
              AbsenceType.CLASS,
              offset % 2 == 0,
              student,
              el1
            )
          );
          if (matchingEls.size() > 1) {
            Element el2 = matchingEls.get(1);
            absences.add(
              new Absence(
                LocalDateTime.now()
                  .minusDays(offset + 3)
                  .withHour(14)
                  .withMinute(0)
                  .withSecond(0)
                  .withNano(0),
                AbsenceType.EXAM,
                false,
                student,
                el2
              )
            );
          }
          offset++;
        }
      }
    }
    return absenceRepository.saveAll(absences);
  }

  private void seedRequests(List<User> users) {
    User student1 = users.get(1);
    User student2 = users.get(2);
    User student3 = users.get(3);
    User teacher1 = users.stream()
      .filter(u -> u.getRole() == Role.TEACHER)
      .findFirst()
      .orElse(student1);

    List<Request> requests = List.of(
      new Request(
        null, "Attestation de Scolarite", RequestState.PENDING,
        new Date(), null, student1
      ),
      new Request(
        null, "Official Transcript Request", RequestState.IN_REVIEW,
        addDays(new Date(), -2), addDays(new Date(), 3), student1
      ),
      new Request(
        null, "Absence Justification Review", RequestState.COMPLETED,
        addDays(new Date(), -5), new Date(), student1
      ),
      new Request(
        null, "Internship Convention Signature", RequestState.READY_FOR_PICKUP,
        addDays(new Date(), -3), addDays(new Date(), 1), student1
      ),
      new Request(
        null, "Course Group Transfer", RequestState.REJECTED,
        addDays(new Date(), -6), addDays(new Date(), -4), student1
      ),
      new Request(
        null, "Exam Retake Petition", RequestState.PENDING,
        new Date(), null, student2
      ),
      new Request(
        null, "Hardware Lab Equipment Loan", RequestState.IN_PROGRESS,
        addDays(new Date(), -3), addDays(new Date(), 2), student2
      ),
      new Request(
        null, "Releve de Notes S1", RequestState.APPROVED,
        addDays(new Date(), -4), addDays(new Date(), 1), student3
      ),
      new Request(
        null, "Projector & HDMI Kit Reservation - Amphi 1",
        RequestState.PENDING, new Date(), null, teacher1
      ),
      new Request(
        null, "Teaching Certification Attestation",
        RequestState.IN_PROGRESS, addDays(new Date(), -2),
        addDays(new Date(), 2), teacher1
      )
    );
    requestRepository.saveAll(requests);
  }

  private void seedMaterials(List<Element> elements) {
    if (elements.isEmpty()) {
      return;
    }

    byte[] samplePng = Base64.getDecoder().decode(
      "iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAFUlEQVQYV2Nk" +
      "YGD4z4AHMDFiAwwMAAAZ+QEF4x8tGQAAAABJRU5ErkJggg=="
    );

    List<Material> materials = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < elements.size(); i++) {
      Element el = elements.get(i);
      User teacher = el.getTeacher();
      String baseName = el.getName().replaceAll("[^a-zA-Z0-9]+", "_");

      MaterialFolder coursDir = materialFolderRepository.save(
        new MaterialFolder("Cours", el, null)
      );
      MaterialFolder chapDir = materialFolderRepository.save(
        new MaterialFolder("Chapitres", el, coursDir)
      );
      MaterialFolder tdDir = materialFolderRepository.save(
        new MaterialFolder("TD & TP", el, null)
      );
      MaterialFolder solDir = materialFolderRepository.save(
        new MaterialFolder("Corrections", el, tdDir)
      );
      MaterialFolder assignDir = materialFolderRepository.save(
        new MaterialFolder("Devoirs", el, null)
      );
      MaterialFolder mediaDir = materialFolderRepository.save(
        new MaterialFolder("Schemas & Images", el, null)
      );

      materials.add(buildMaterial(
        "Syllabus - " + el.getName(),
        "Course outline, grading weights, and bibliography.",
        baseName + "_syllabus.pdf",
        "application/pdf",
        FileType.PDF,
        ("Syllabus for " + el.getName()).getBytes(StandardCharsets.UTF_8),
        false,
        null,
        el,
        null,
        teacher,
        now.minusDays(20)
      ));

      materials.add(buildMaterial(
        "Part 1 - Foundations",
        "Core theory and introductory slides.",
        "part1.pdf",
        "application/pdf",
        FileType.PDF,
        ("Part 1 lecture notes: " + el.getName())
          .getBytes(StandardCharsets.UTF_8),
        false,
        null,
        el,
        chapDir,
        teacher,
        now.minusDays(15)
      ));

      materials.add(buildMaterial(
        "Part 2 - Architecture & Case Study",
        "Detailed presentation deck.",
        "part2.pptx",
        "application/vnd.openxmlformats-officedocument" +
          ".presentationml.presentation",
        FileType.PPTX,
        ("Part 2 slides: " + el.getName()).getBytes(StandardCharsets.UTF_8),
        false,
        null,
        el,
        chapDir,
        teacher,
        now.minusDays(10)
      ));

      materials.add(buildMaterial(
        "Lab Sheet 1",
        "Guided practical exercises.",
        "tp1_guide.docx",
        "application/vnd.openxmlformats-officedocument" +
          ".wordprocessingml.document",
        FileType.DOCX,
        ("Lab 1 instructions for " + el.getName())
          .getBytes(StandardCharsets.UTF_8),
        false,
        null,
        el,
        tdDir,
        teacher,
        now.minusDays(8)
      ));

      materials.add(buildMaterial(
        "TP1 Starter Code",
        "Reference source template.",
        "starter.txt",
        "text/plain",
        FileType.SOURCE_CODE,
        ("Starter template for " + el.getName())
          .getBytes(StandardCharsets.UTF_8),
        false,
        null,
        el,
        solDir,
        teacher,
        now.minusDays(6)
      ));

      materials.add(buildMaterial(
        "Architecture Diagram",
        "High-level visual reference.",
        "diagram.png",
        "image/png",
        FileType.IMAGE,
        samplePng,
        false,
        null,
        el,
        mediaDir,
        teacher,
        now.minusDays(5)
      ));

      materials.add(buildMaterial(
        "Assignment 1 - Mini Project",
        "Submit your report and source archive before the deadline.",
        "assignment1_brief.pdf",
        "application/pdf",
        FileType.PDF,
        ("Assignment 1 brief for " + el.getName())
          .getBytes(StandardCharsets.UTF_8),
        true,
        now.plusDays(4 + (i % 10)).withHour(23).withMinute(59),
        el,
        assignDir,
        teacher,
        now.minusDays(3)
      ));

      if (i % 2 == 0) {
        materials.add(buildMaterial(
          "Assignment 2 - Lab Report",
          "Past submission brief for reference.",
          "lab_report_template.docx",
          "application/vnd.openxmlformats-officedocument" +
            ".wordprocessingml.document",
          FileType.DOCX,
          ("Assignment 2 template for " + el.getName())
            .getBytes(StandardCharsets.UTF_8),
          true,
          now.minusDays(2).withHour(23).withMinute(59),
          el,
          assignDir,
          teacher,
          now.minusDays(12)
        ));
      }

      if (i % 3 == 0) {
        materials.add(buildMaterial(
          "Dataset & Archive Bundle",
          "Compressed resources for lab sessions.",
          "resources_bundle.zip",
          "application/zip",
          FileType.ZIP,
          ("Archive placeholder for " + el.getName())
            .getBytes(StandardCharsets.UTF_8),
          false,
          null,
          el,
          tdDir,
          teacher,
          now.minusDays(4)
        ));
      }
    }

    materialRepository.saveAll(materials);
  }

  private Material buildMaterial(
    String title,
    String description,
    String fileName,
    String contentType,
    FileType fileType,
    byte[] data,
    boolean assignment,
    LocalDateTime deadline,
    Element element,
    MaterialFolder folder,
    User uploadedBy,
    LocalDateTime createdAt
  ) {
    Material m = new Material();
    m.setTitle(title);
    m.setDescription(description);
    m.setOriginalFileName(fileName);
    m.setContentType(contentType);
    m.setFileType(fileType);
    m.setFileData(data);
    m.setFileSize(data != null ? data.length : 0L);
    m.setAssignment(assignment);
    m.setDeadline(deadline);
    m.setElement(element);
    m.setFolder(folder);
    m.setUploadedBy(uploadedBy);
    m.setCreatedAt(createdAt);
    return m;
  }

  private Date addDays(Date date, int days) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_YEAR, days);
    return cal.getTime();
  }

  private void ensureRootAdmin() {
    List<User> admins = userRepository.findByRole(Role.ADMIN);
    boolean hasRoot = admins.stream().anyMatch(User::isRootAdmin);
    if (!hasRoot && !admins.isEmpty()) {
      User root = admins.get(0);
      root.setRootAdmin(true);
      userRepository.save(root);
    }
  }
}
