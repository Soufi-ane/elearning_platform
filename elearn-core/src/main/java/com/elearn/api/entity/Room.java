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
public class Room {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String label;
  private int capacity;
  private int floor;
  private RoomType type;

  @ManyToOne()
  @JoinColumn(name = "campus_id")
  private Campus campus;

  @OneToMany(mappedBy = "room")
  private List<DayPlan> plannings;

  public Room(String label, int capacity, int floor, RoomType type, Campus campus){
    this.label = label;
    this.capacity = capacity;
    this.floor = floor;
    this.campus = campus;
  }
}

