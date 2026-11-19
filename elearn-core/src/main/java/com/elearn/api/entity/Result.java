package com.elearn.api.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(
    columnNames = {"student_id", "element_id"}
  )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private double grade;

  private String remarks;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @ManyToOne
  @JoinColumn(name = "element_id", nullable = false)
  private Element element;

  @ManyToOne
  @JoinColumn(name = "graded_by_id")
  private User gradedBy;

  @ManyToOne
  @JoinColumn(name = "last_modified_by_id")
  private User lastModifiedBy;

  public Result(double grade, User student, Element element) {
    this(
      grade,
      student,
      element,
      element != null ? element.getTeacher() : null,
      null
    );
  }

  public Result(
    double grade,
    User student,
    Element element,
    User gradedBy,
    String remarks
  ) {
    this.grade = grade;
    this.student = student;
    this.element = element;
    this.gradedBy = gradedBy;
    this.lastModifiedBy = gradedBy;
    this.remarks = remarks;
  }

  public String computeStatus() {
    if (this.grade >= 10.0) {
      return "PASSED";
    }
    if (this.grade >= 7.0) {
      return "RETAKE";
    }
    return "FAILED";
  }

  @PrePersist
  public void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (this.createdAt == null) {
      this.createdAt = now;
    }
    this.updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
