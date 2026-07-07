package com.elearn.api.entity;

public enum DepartmentName {

  GENIE_INFORMATIQUE("Génie Informatique", 6),
  GENIE_ELECTRIQUE("Génie Électrique", 6),
  GENIE_CIVIL("Génie Civil", 6);

  private final String displayName;
  private final int totalSemesters;

  DepartmentName(String displayName, int totalSemesters) {
    this.displayName = displayName;
    this.totalSemesters = totalSemesters;
  }

  public String getName() {
    return displayName;
  }

  public int getTotalSemesters() {
    return totalSemesters;
  }

}
