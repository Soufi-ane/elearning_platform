package com.elearn.api.entity;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String name;
  private int year;
  private int semester;

  @Enumerated(EnumType.STRING)
  private DepartmentName department;

  @OneToMany(mappedBy = "module")
  private List<Element> elements;

  public Module(
    String name,
    int year,
    int semester,
    DepartmentName department
  ) {
    this.name = name;
    this.year = year;
    this.semester = semester;
    this.department = department;
  }

  public Module(String name, int semester, DepartmentName department) {
    this(name, 1, semester, department);
  }
}
