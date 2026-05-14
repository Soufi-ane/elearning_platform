package com.elearn.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private double grade;

  @ManyToOne()
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne()
  @JoinColumn(name = "element_id")
  private Element element;

  public Result(double grade, User student, Element element){
    this.grade = grade;
    this.student = student;
    this.element = element;
  }

}

