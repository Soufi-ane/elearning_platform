package com.elearn.api.config;

import java.time.LocalDate;
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

  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;
  private final ModuleRepository moduleRepository;
  private final ElementRepository elementRepository;
  private final DayPlanRepository dayPlanRepository;
  private final CampusRepository campusRepository;
  private final RoomRepository roomRepository;
  private final PasswordEncoder passwordEncoder;
  private final AbsenceRepository absenceRepository;

  @Override
  public void run(String... args) throws Exception {
    if(departmentRepository.count() == 0) {
      System.out.println("Seeding data ...");
      List<Department> departments = seedDepartments();
      List<User> users = seedUsers(departments);
      List<Module> modules = seedModules(departments);
      List<Element> elements = seedElements(modules,users);
      if(campusRepository.count() == 0) {
        List<Campus> campuses = seedCampuses();
        List<Room> rooms = seedRooms(campuses);
        seedTimeTables(elements,rooms);
      }
      if(absenceRepository.count() == 0) seedAbsence(users,elements);
    }
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

  private List<Department> seedDepartments(){
    List<Department> departments = List.of(
      new Department("Génie Informatique"),
      new Department("Génie Électrique"),
      new Department("Génie Civil")
    );
    return departmentRepository.saveAll(departments);
  }

  private List<User> seedUsers(List<Department> departments){
    List<User> users = List.of(
      new User(
        "Abujad", "abdellah", "abujad", null, "abujad@supmti.ac.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(1990, 5, 15),
        Role.ADMIN, null, 0
      ),
      new User(
        "soufiane", "jaber", "soufiane", departments.get(0), "soufianejb@mail.ma",
        passwordEncoder.encode("12341234"), LocalDate.of(2005, 11, 12),
        Role.STUDENT, StudyMode.HYBRID, 1
      ),
      new User(
        "Yasmine", "Tazi", "yasmine-t", departments.get(1), "tazi.yasmine@um5.ac.ma",
        passwordEncoder.encode("qwerty"), LocalDate.of(2002, 10, 10),
        Role.STUDENT, StudyMode.ON_SITE, 2
      ),
      new User(
        "Rachid", "Saadane", "saadane", departments.get(0), "saadane@supmti.ac.ma",
        passwordEncoder.encode("saadane1324"), LocalDate.of(1985, 1, 5), 
        Role.TEACHER, null,0
      ),
      new User(
        "Ahmed", "Zellou", "zellou", departments.get(0), "ahmed.zellou@ensias.ac.ma",
        passwordEncoder.encode("zellou1234"), LocalDate.of(1985, 1, 5),
        Role.TEACHER, null,0
      )
    );
    return userRepository.saveAll(users);
  }

  private List<Module> seedModules(List<Department> departments){
    List<Module> modules = List.of(
      new Module("Programmation Resaux",1,departments.get(0)),
      new Module("Programmation Python",1,departments.get(0)),
      new Module("Programmation OOP",1,departments.get(0)),
      new Module("Base De donnee",1,departments.get(0)),
      new Module("Langues entrangères",1,departments.get(0)),

      new Module("CMS",1,departments.get(0)),
      new Module("Génie Logiciel",1,departments.get(0)),

      new Module("UML",1,departments.get(0)),

      //s2
      new Module("Mysql",2,departments.get(0)),
      new Module("Python Machine learning",2,departments.get(0)),
      new Module("Java EE",2,departments.get(0)),
      new Module("ENGLISH 2",2,departments.get(0)),
      new Module("FRENCH 2",2,departments.get(0)),
      new Module("UML 2",2,departments.get(0)),
      new Module("Project interdisciplinaire",2,departments.get(0)),
      new Module("Base de donnee Oracle",2,departments.get(0)),
      new Module("Inteligence Artificielle",2,departments.get(0))
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

}
