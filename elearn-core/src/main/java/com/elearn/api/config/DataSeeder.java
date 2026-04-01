package com.elearn.api.config;

import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.elearn.api.entity.Department;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.repository.DepartmentRepository;
import com.elearn.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;

  @Override
  public void run(String... args) throws Exception {
    List<Department> departments = seedDepartments();
    seedUsers(departments);
    System.out.println("test is working\n");
  }

  private List<Department> seedDepartments(){
    List<Department> departments = List.of(
      new Department("Génie Informatique"),
      new Department("Génie Électrique"),
      new Department("Génie Civil")
    );
    if(departmentRepository.count() == 0) {
      departmentRepository.saveAll(departments);
      System.out.println("departments seeded");
    } else {
      System.out.println("departments already seeded");
    }
    return departments;
  }

  private void seedUsers(List<Department> departments){
    List<User> users = List.of(
      new User(
        "Omar", "Mansouri", "admin-omar", departments.get(0),
        "o.mansouri@edu.ma", "admin_secure", LocalDate.of(2004, 3, 12), Role.STUDENT
      ),
      new User(
        "Amine", "Bennani", "amine-ben", departments.get(1),
        "amine.bennani@emsi.ma", "pass123", LocalDate.of(2005, 5, 15), Role.STUDENT
      ),
      new User(
        "Yasmine", "Tazi", "yasmine-t", departments.get(1),
        "tazi.yasmine@um5.ac.ma", "qwerty", LocalDate.of(2002, 10, 10), Role.STUDENT
      ),
      new User(
        "Mehdi", "Alaoui", "mehdi-77", departments.get(2),
        "m.alaoui@gmail.com", "mehdi2026", LocalDate.of(2000, 1, 5), Role.STUDENT
      ),
      new User(
        "Sara", "El Idrissi", "sara-idrissi",departments.get(2),
        "sara.idrissi@gmail.com", "sara99", LocalDate.of(2005, 8, 22), Role.STUDENT
      )
    );
    if(userRepository.count() == 0) {
      userRepository.saveAll(users);
      System.out.println("user seeded");
    } else {
      System.out.println("users already seeded");
    }
  }
}
