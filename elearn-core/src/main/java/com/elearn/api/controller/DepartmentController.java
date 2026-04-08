package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.Schemas.DepartmentBaseResponse;
import com.elearn.api.service.DepartmentService;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {
  private final DepartmentService departmentService;

  @Autowired
  public DepartmentController(DepartmentService departmentService){
    this.departmentService = departmentService;
  }

  @GetMapping
  public List<DepartmentBaseResponse> findAll(){
    return departmentService.findAll();
  }

}
