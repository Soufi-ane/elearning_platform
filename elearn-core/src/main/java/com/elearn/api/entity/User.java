package com.elearn.api.entity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.elearn.api.entity.Schemas.RegisterRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "platform_users")
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String firstName;
  private String lastName;
  private String username;
  private String email;
  private String password;
  private LocalDate dateOfBirth;
  private StudyMode studyMode;
  private int year;
  private int semester;
  private int numSemesters;

  public User(
    String firstName, String lastName, String username, DepartmentName department,
    String email, String password, LocalDate dateOfBirth, 
    Role role,StudyMode studyMode, int year, int semester
  ){
    this(firstName,lastName,username,email,password,dateOfBirth,role,studyMode,year,semester);
    this.department = department;
    this.numSemesters = department != null ?
    department.getTotalSemesters() : 0;
  }

  public User(RegisterRequest request, String encodedPassword){
    this(
      request.firstName(),request.lastName(),request.username(),
      request.email(), encodedPassword, request.dateOfBirth(),
      request.role(),request.studyMode(),request.year(), request.semester()
    );
  }

  public User(
    String firstName, String lastName, String username,
    String email, String password, LocalDate dateOfBirth,
    Role role, StudyMode studyMode, int year, int semester
  ){
    this.firstName = firstName;
    this.lastName = lastName;
    this.username = username;
    this.email = email;
    this.password = password;
    this.dateOfBirth = dateOfBirth;
    this.role = role;
    this.studyMode = studyMode;
    this.year = year;
    this.semester = semester;
  }

  @Enumerated(EnumType.STRING)
  private Role role;

  @Enumerated(EnumType.STRING)
  private DepartmentName department;

  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
  private List<Absence> absences;

  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
  private List<Result> results;

  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
  private List<Request> requests;

  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
  private List<Payment> payments;

  @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
  private List<Element> elements;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername(){
    return this.email;
  }

  public String getDbUsername(){
    return this.username;
  }

}

