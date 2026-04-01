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
public class Element {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String name;

  @ManyToOne()
  @JoinColumn(name = "module_id")
  private Module module;

  @OneToMany(mappedBy = "element")
  private List<Absence> absences;

  @OneToMany(mappedBy = "element")
  private List<Result> results;

  @ManyToOne()
  @JoinColumn(name = "teacher_id")
  private User teacher;
}

