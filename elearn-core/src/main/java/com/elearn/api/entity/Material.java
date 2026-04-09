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
public class Material {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String title;
  private FileType fileType; 
  private String fileUrl; 
  private LocalDate createdAt;
  private LocalDate deadline;

  @ManyToOne
  @JoinColumn(name = "element_id")
  private Element element;
}
