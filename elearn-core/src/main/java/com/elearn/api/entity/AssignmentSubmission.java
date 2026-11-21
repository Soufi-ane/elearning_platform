package com.elearn.api.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "assignment_id", "student_id" }
  )
)
public class AssignmentSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String originalFileName;
  private String contentType;
  private long fileSize;

  @Column(name = "file_data", columnDefinition = "bytea")
  private byte[] fileData;

  private LocalDateTime submittedAt;
  private boolean late;

  @Column(length = 1000)
  private String comment;

  @ManyToOne
  @JoinColumn(name = "assignment_id")
  private Material assignment;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;
}
