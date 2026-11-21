package com.elearn.api.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialFolder {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  private String name;
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "element_id")
  private Element element;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private MaterialFolder parent;

  @OneToMany(
    mappedBy = "parent",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private List<MaterialFolder> subFolders = new ArrayList<>();

  @OneToMany(
    mappedBy = "folder",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private List<Material> materials = new ArrayList<>();

  public MaterialFolder(
    String name,
    Element element,
    MaterialFolder parent
  ) {
    this.name = name;
    this.element = element;
    this.parent = parent;
    this.createdAt = LocalDateTime.now();
  }
}
