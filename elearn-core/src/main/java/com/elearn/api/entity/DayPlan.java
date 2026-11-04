package com.elearn.api.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
  private LocalTime startsAt;
  private LocalTime endsAt;
  private int weeklyRepeats;

  @Enumerated(EnumType.STRING)
  private PlanType type;

  @Enumerated(EnumType.STRING)
  private DepartmentName department;

  private int year;
  private int semester;
  private String groupName;

  @ManyToOne
  @JoinColumn(name = "element_id")
  private Element element;

  @ManyToOne
  @JoinColumn(name = "room_id")
  private Room room;

  public DayPlan(
    LocalDate date,
    LocalTime startsAt,
    LocalTime endsAt,
    int weeklyRepeats,
    PlanType type,
    DepartmentName department,
    int year,
    int semester,
    String groupName,
    Element element,
    Room room
  ) {
    this.date = date;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.weeklyRepeats = weeklyRepeats;
    this.type = type;
    this.department = department;
    this.year = year;
    this.semester = semester;
    this.groupName = groupName;
    this.element = element;
    this.room = room;
  }
}
