package com.elearn.api.entity;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String name;
  private int semester;

  @ManyToOne()
  @JoinColumn(name = "department_id")
  private Department department;

  @OneToMany(mappedBy = "module")
  private List<Element> elements;

  public Module(String name, int semester, Department department){
    this.name = name;
    this.semester = semester;
    this.department = department;
  }

}

