package com.elearn.api.entity;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String firstName;
  private String lastName;
  private String username;
  private String email;
  private String password;
  private LocalDate dateOfBirth;

  public User(
    String firstName, String lastName, String username, Department department,
    String email, String password, LocalDate dateOfBirth, Role role
  ){
    this.firstName = firstName;
    this.lastName = lastName;
    this.username = username;
    this.department = department;
    this.email = email;
    this.password = password;
    this.dateOfBirth = dateOfBirth;
    this.role = role;
  }

  @Enumerated(EnumType.STRING)
  private Role role;

  @ManyToOne()
  @JoinColumn(name = "department_id")
  private Department department;

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
}

