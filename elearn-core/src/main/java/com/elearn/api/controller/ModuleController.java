package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementRequest;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleRequest;
import com.elearn.api.entity.Schemas.ModuleResponse;
import com.elearn.api.service.ModuleService;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {
  private final ModuleService moduleService;

  @Autowired
  public ModuleController(ModuleService moduleService) {
    this.moduleService = moduleService;
  }

  @GetMapping
  public ResponseEntity<List<ModuleResponse>> getMyModules(
    @AuthenticationPrincipal User user,
    @RequestParam(required = false) DepartmentName department,
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer semester
  ) {
    return ResponseEntity.ok(
      moduleService.getModulesForUser(user, department, year, semester)
    );
  }

  @GetMapping("/{moduleId}/elements")
  public ResponseEntity<List<ElementResponse>> getElements(
    @PathVariable String moduleId,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(
      moduleService.getElementsForModule(moduleId, user)
    );
  }

  @GetMapping("/elements")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<List<ElementResponse>> getAllElements(
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(moduleService.getAllElements(user));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ModuleResponse> createModule(
    @RequestBody ModuleRequest request
  ) {
    return ResponseEntity.ok(moduleService.createModule(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ModuleResponse> updateModule(
    @PathVariable String id,
    @RequestBody ModuleRequest request
  ) {
    return ResponseEntity.ok(moduleService.updateModule(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteModule(@PathVariable String id) {
    moduleService.deleteModule(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/elements")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ElementResponse> createElement(
    @RequestBody ElementRequest request
  ) {
    return ResponseEntity.ok(moduleService.createElement(request));
  }

  @PutMapping("/elements/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ElementResponse> updateElement(
    @PathVariable String id,
    @RequestBody ElementRequest request
  ) {
    return ResponseEntity.ok(moduleService.updateElement(id, request));
  }

  @DeleteMapping("/elements/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteElement(@PathVariable String id) {
    moduleService.deleteElement(id);
    return ResponseEntity.ok().build();
  }
}
