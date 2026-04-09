package com.elearn.api.entity;

import java.time.LocalDate;
import java.time.LocalTime;
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
  private LocalTime startsAt;
  private LocalTime endsAt;
  private int weeklyRepeats;
  private PlanType type; 

  @ManyToOne
  @JoinColumn(name = "element_id")
  private Element element;

  public DayPlan( 
    LocalDate date, LocalTime startsAt, LocalTime endsAt,
    int weeklyRepeats, PlanType type, Element element, Room room
  ){
    this.date = date;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.weeklyRepeats = weeklyRepeats;
    this.type = type;
    this.element = element;
    this.room = room;
  }

  @ManyToOne()
  @JoinColumn(name = "room_id")
  private Room room;

}
