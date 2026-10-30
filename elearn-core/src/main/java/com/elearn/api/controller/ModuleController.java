package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleResponse;
import com.elearn.api.entity.Schemas.ModuleRequest;
import com.elearn.api.entity.Schemas.ElementRequest;
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
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(moduleService.getModulesForUser(user));
  }

  @GetMapping("/{moduleId}/elements")
  public ResponseEntity<List<ElementResponse>> getElements(
    @PathVariable String moduleId, 
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(moduleService.getElementsForModule(moduleId, user));
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
