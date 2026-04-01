package com.elearn.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.repository.DepartmentRepository;

@Service 
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  @Autowired
  public DepartmentService(DepartmentRepository departmentRepository){
    this.departmentRepository = departmentRepository;
  }
}
