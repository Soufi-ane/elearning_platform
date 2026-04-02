package com.elearn.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Schemas.DepartmentBaseResponse;
import com.elearn.api.repository.DepartmentRepository;

@Service 
public class DepartmentService {
  private final DepartmentRepository departmentRepository;

  @Autowired
  public DepartmentService(DepartmentRepository departmentRepository){
    this.departmentRepository = departmentRepository;
  }

  public List<DepartmentBaseResponse> findAll(){
    return departmentRepository.findAll()
      .stream()
      .map(department -> 
          new DepartmentBaseResponse(department.getId(),department.getName())
      ).toList();
  }
}
