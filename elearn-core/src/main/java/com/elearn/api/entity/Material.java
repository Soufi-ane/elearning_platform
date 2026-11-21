package com.elearn.api.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;
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
public class Material {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String title;

  @Column(length = 1000)
  private String description;

  private String originalFileName;
  private String contentType;
  private long fileSize;

  @Enumerated(EnumType.STRING)
  private FileType fileType;

  @Column(name = "file_data", columnDefinition = "bytea")
  private byte[] fileData;

  private boolean assignment;
  private LocalDateTime createdAt;
  private LocalDateTime deadline;

  @ManyToOne
  @JoinColumn(name = "element_id")
  private Element element;

  @ManyToOne
  @JoinColumn(name = "folder_id")
  private MaterialFolder folder;

  @ManyToOne
  @JoinColumn(name = "uploaded_by_id")
  private User uploadedBy;

  @OneToMany(
    mappedBy = "assignment",
    cascade = jakarta.persistence.CascadeType.ALL,
    orphanRemoval = true
  )
  private java.util.List<AssignmentSubmission> submissions = new java.util.ArrayList<>();
}
