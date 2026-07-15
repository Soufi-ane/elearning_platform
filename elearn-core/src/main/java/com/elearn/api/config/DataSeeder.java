package com.elearn.api.config;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

  @Override
  public void run(String... args) throws Exception {
    if (args.length > 0 && "seed".equalsIgnoreCase(args[0])) {
      System.out.println("Seeding data ...");
      dayPlanRepository.deleteAllInBatch();
      absenceRepository.deleteAllInBatch();
      resultRepository.deleteAllInBatch();
      elementRepository.deleteAllInBatch();
      roomRepository.deleteAllInBatch();
      moduleRepository.deleteAllInBatch();
      userRepository.deleteAllInBatch();
      campusRepository.deleteAllInBatch();
      requestRepository.deleteAllInBatch();

      List<User> users = seedUsers();
      List<Module> modules = seedModules();
      List<Element> elements = seedElements(modules,users);
      seedResults(elements, users);
      List<Campus> campuses = seedCampuses();
      List<Room> rooms = seedRooms(campuses);
      seedTimeTables(elements,rooms);
      seedAbsence(users,elements);
      seedRequests(users);
    }
  }

  private void seedResults(List<Element> elements, List<User> users){
    List<Result> results = List.of(
      new Result(14, users.get(1), elements.get(0)),
      new Result(17, users.get(1), elements.get(1)),
      new Result(9, users.get(1), elements.get(2)),
      new Result(4, users.get(1), elements.get(3))
    );
    resultRepository.saveAll(results);
  }

  private List<Absence> seedAbsence(List<User> users, List<Element> elements){
    List<Absence> absences = List.of(
      new Absence(
        LocalDateTime.now(),AbsenceType.CLASS,
        true,users.get(1),elements.get(0)
      ),
      new Absence(
        LocalDateTime.now(),AbsenceType.EXAM,
        false,users.get(1),elements.get(3)
      ),
      new Absence(
        LocalDateTime.now(),AbsenceType.CLASS,
        false,users.get(1),elements.get(6)
      ),
      new Absence(
        LocalDateTime.now().minusDays(5),AbsenceType.CLASS,
        false,users.get(1),elements.get(6)
      ),
      new Absence(
        LocalDateTime.now(),AbsenceType.EXAM,
        false,users.get(2),elements.get(1)
      ),
      new Absence(
        LocalDateTime.now().minusDays(2),AbsenceType.CLASS,
        true,users.get(2),elements.get(1)
      ),
      new Absence(
        LocalDateTime.now(),AbsenceType.EXAM,
        false,users.get(2),elements.get(6)
      )
    );
    return absenceRepository.saveAll(absences);
  }

  private List<User> seedUsers(){
    List<User> users = List.of(
      new User(
        "Abujad", "abdellah", "abujad", null, "abujad@supmti.ac.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1990, 5, 15),
        Role.ADMIN, null, 0,0
      ),
      new User(
        "soufiane", "jaber", "soufiane", DepartmentName.GENIE_INFORMATIQUE,
        "soufianejb@mail.ma", passwordEncoder.encode("12341234"), LocalDate.of(2005, 11, 12),
        Role.STUDENT, StudyMode.HYBRID, 1, 3
      ),
      new User(
        "Yasmine", "Tazi", "yasmine-t",  DepartmentName.GENIE_CIVIL, "tazi.yasmine@um5.ac.ma",
        passwordEncoder.encode("qwerty"), LocalDate.of(2002, 10, 10),
        Role.STUDENT, StudyMode.ON_SITE, 2, 2
      ),
      new User(
        "Rachid", "Saadane", "saadane", DepartmentName.GENIE_INFORMATIQUE, "saadane@supmti.ac.ma",
        passwordEncoder.encode("saadane1324"), LocalDate.of(1985, 1, 5), 
        Role.TEACHER, null, 0, 0
      ),
      new User(
        "Ahmed", "Zellou", "zellou", DepartmentName.GENIE_INFORMATIQUE, "ahmed.zellou@ensias.ac.ma",
        passwordEncoder.encode("zellou1234"), LocalDate.of(1985, 1, 5),
        Role.TEACHER, null, 0 , 0
      )
    );
    return userRepository.saveAll(users);
  }

  private List<Module> seedModules(){
    List<Module> modules = List.of(
      new Module("Programmation Resaux",1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Programmation Python",1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Programmation OOP",1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Base De donnee",1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Langues entrangères",1, DepartmentName.GENIE_INFORMATIQUE),

      new Module("CMS",1, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Génie Logiciel",1, DepartmentName.GENIE_INFORMATIQUE),

      new Module("UML",1, DepartmentName.GENIE_INFORMATIQUE),

      //s2
      new Module("Mysql",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Python Machine learning",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Java EE",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("ENGLISH 2",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("FRENCH 2",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("UML 2",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Project interdisciplinaire",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Base de donnee Oracle",2, DepartmentName.GENIE_INFORMATIQUE),
      new Module("Inteligence Artificielle",2, DepartmentName.GENIE_INFORMATIQUE)
    );
    return moduleRepository.saveAll(modules);
  }

  private List<Element> seedElements(List<Module> modules,List<User> users){
    List<Element> elements = List.of(
      new Element("TCP/IP",modules.get(0),users.get(3)),
      new Element("Programmation client serveur",modules.get(0),users.get(4)),

      new Element("Data Science",modules.get(1),users.get(3)),
      new Element("Python POO",modules.get(1),users.get(3)),

      new Element("Java POO",modules.get(2),users.get(3)),
      new Element("UML",modules.get(2),users.get(3)),

      new Element("MSSQL Server",modules.get(3),users.get(3)),

      new Element("ENGLISH 1",modules.get(4),users.get(3)),
      new Element("FRENCH 1",modules.get(4),users.get(3)),

      new Element("CMS",modules.get(5),users.get(3)),

      new Element("Génie Logiciel",modules.get(6),users.get(3))
    );
    return elementRepository.saveAll(elements);
  }

  private List<DayPlan> seedTimeTables(List<Element> elements, List<Room> rooms){
    List<DayPlan> plans = List.of(
      new DayPlan(LocalDate.of(2026, 4, 6), LocalTime.of(9, 0),
        LocalTime.of(11, 0),0 ,PlanType.LECTURE,elements.get(0), rooms.get(3)
      ),
      new DayPlan(LocalDate.of(2026, 4, 6), LocalTime.of(14, 30),
        LocalTime.of(16, 30),14 ,PlanType.LECTURE,elements.get(1), rooms.get(0)
      ),
      new DayPlan(LocalDate.of(2026, 4, 7), LocalTime.of(9, 0),
        LocalTime.of(11, 0),14 , PlanType.LECTURE,elements.get(2), rooms.get(0)
      ),
      new DayPlan(LocalDate.of(2026, 4, 7), LocalTime.of(14, 0),
        LocalTime.of(16, 0),14 ,PlanType.LECTURE,elements.get(3), rooms.get(1)
      ),
      new DayPlan(LocalDate.of(2026, 4, 8), LocalTime.of(8, 30),
        LocalTime.of(11, 0),14 ,PlanType.LECTURE,elements.get(4), rooms.get(1)
      ),
      new DayPlan(LocalDate.of(2026, 4, 8), LocalTime.of(16, 0),
        LocalTime.of(18, 0),14 , PlanType.LECTURE,elements.get(5), rooms.get(1)
      ),
      new DayPlan(LocalDate.of(2026, 4, 9), LocalTime.of(10, 30),
        LocalTime.of(12, 30),14 , PlanType.LECTURE,elements.get(6), rooms.get(1)
      ),
      new DayPlan(LocalDate.of(2026, 4, 9), LocalTime.of(15, 30),
        LocalTime.of(17, 30),14 , PlanType.LECTURE,elements.get(7), rooms.get(2)
      ),
      new DayPlan(LocalDate.of(2026, 4, 10), LocalTime.of(8, 30),
        LocalTime.of(10, 30),14 , PlanType.LECTURE,elements.get(8), rooms.get(2)
      ),
      new DayPlan(LocalDate.of(2026, 4, 10), LocalTime.of(15, 0),
        LocalTime.of(17, 0),14 , PlanType.LECTURE,elements.get(9), rooms.get(0)
      ),
      new DayPlan(LocalDate.of(2026, 4, 11), LocalTime.of(8, 30),
        LocalTime.of(10, 30),14 , PlanType.LECTURE,elements.get(10), rooms.get(1)
      )
    );
    return dayPlanRepository.saveAll(plans);
  }

  private List<Campus> seedCampuses(){
    List<Campus> campuses = List.of(
      new Campus("Agdal 1","22 Av. Omar Ibn Al Khattab, Rabat 10090"),
      new Campus("Medina 1","98 Av. Allal Ben Abdellah, Rabat 10000")
    );
    return campusRepository.saveAll(campuses);
  }

  private List<Room> seedRooms(List<Campus> campuses){
    List<Room> rooms = List.of(
      new Room("TP2", 35, 3, RoomType.TP, campuses.get(0)),
      new Room("AMPHI 7", 50, 3, RoomType.AMPHI, campuses.get(0)),
      new Room("AMPHI 3", 50, 3, RoomType.AMPHI, campuses.get(1)),
      new Room("Salle Polyvalente", 120, 0, RoomType.POLYVALENTE, campuses.get(1))
    );
    return roomRepository.saveAll(rooms);
  }
  
  private void seedRequests(List<User> users) {
    User student1 = users.get(1);
    User student2 = users.get(2);

    List<Request> requests = List.of(
      new Request(null, "Document Request A", RequestState.PENDING, new Date(), null, student1),
      new Request(null, "Transcript Request", RequestState.IN_PROGRESS, addDays(new Date(), -2), null, student1),
      new Request(
        null, "Absence Justification", RequestState.COMPLETED, 
        addDays(new Date(), -5), new Date(), student1
      ),
      new Request(null, "Library Access", RequestState.PENDING, new Date(), null, student1),
      new Request(null, "Course Change", RequestState.IN_PROGRESS, addDays(new Date(), -1), null, student1),
      
      new Request(null, "Exam Retake", RequestState.PENDING, new Date(), null, student2),
      new Request(
        null, "Medical Certificate", RequestState.COMPLETED, 
        addDays(new Date(), -10), addDays(new Date(), -8), student2
      ),
      new Request(null, "Hardware Loan", RequestState.IN_PROGRESS, addDays(new Date(), -3), null, student2),
      new Request(null, "Study Abroad Inquiry", RequestState.PENDING, new Date(), null, student2),
      new Request(
        null, "Software License Request", RequestState.COMPLETED,
        addDays(new Date(), -7), addDays(new Date(), -6), student2
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
