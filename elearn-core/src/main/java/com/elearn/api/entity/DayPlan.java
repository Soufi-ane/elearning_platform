package com.elearn.api.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private LocalDate date;
  private LocalDate startsAt;
  private LocalDate endsAt;
  private int weeklyRepeats;
  private PlanType type; 

  @ManyToOne
  @JoinColumn(name = "element_id")
  private Element element;
}
