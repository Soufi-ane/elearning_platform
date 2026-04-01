package com.elearn.api.entity;

import java.time.LocalDateTime;
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
public class Absence {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private LocalDateTime dateTime;
  private AbsenceType type;
  private boolean isJustified;

  @ManyToOne()
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne()
  @JoinColumn(name = "element_id")
  private Element element;
}

