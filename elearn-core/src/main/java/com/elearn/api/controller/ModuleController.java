package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleResponse;
import com.elearn.api.service.ModuleService;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {

  private final ModuleService moduleService;

  @Autowired
  public ModuleController(ModuleService moduleService){
    this.moduleService = moduleService;
  }

  @GetMapping
  public ResponseEntity<List<ModuleResponse>> getMyModules(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(moduleService.getModulesForUser(user));
  }

  @GetMapping("/{moduleId}/elements")
  public ResponseEntity<List<ElementResponse>> getElements(
    @PathVariable String moduleId, 
    @AuthenticationPrincipal User user) 
  {
    return ResponseEntity.ok(moduleService.getElementsForModule(moduleId, user));
  }
}

