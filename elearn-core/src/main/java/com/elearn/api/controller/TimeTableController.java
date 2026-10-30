package com.elearn.api.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.DayPlanRequest;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.service.DayPlanService;

@RestController
@RequestMapping("/api/v1/timeTable")
public class TimeTableController {
  private final DayPlanService dayPlanService;

  @Autowired
  public TimeTableController(DayPlanService dayPlanService) {
    this.dayPlanService = dayPlanService;
  }

  @GetMapping("/{startDate}")
  public Map<LocalDate, List<PlanResponse>> getPlanningByRange(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
    LocalDate startDate,
    @RequestParam(value = "department") DepartmentName department
  ) {
    User user = (User) userDetails;
    boolean isAdmin = user.getRole() == Role.ADMIN;
    boolean isDepartmentValid = false;
    
    if(!isAdmin) {
      isDepartmentValid = user.getDepartment() == department;
    }
    if(!isAdmin && !isDepartmentValid) {
      return new HashMap<>();
    }
    
    return dayPlanService.getByWeek(startDate);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<PlanResponse> createPlan(
    @RequestBody DayPlanRequest request, 
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(dayPlanService.createPlan(request, user));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<PlanResponse> updatePlan(
    @PathVariable String id, 
    @RequestBody DayPlanRequest request, 
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(dayPlanService.updatePlan(id, request, user));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<Void> deletePlan(
    @PathVariable String id, 
    @AuthenticationPrincipal User user
  ) {
    dayPlanService.deletePlan(id, user);
    return ResponseEntity.ok().build();
  }
}
