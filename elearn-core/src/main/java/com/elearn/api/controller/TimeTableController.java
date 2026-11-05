package com.elearn.api.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.DayPlanRequest;
import com.elearn.api.entity.Schemas.DayPlanScheduleRequest;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.service.DayPlanService;

@RestController
@RequestMapping({"/api/v1/timeTable", "/api/timeTable"})
public class TimeTableController {

  private final DayPlanService dayPlanService;

  @Autowired
  public TimeTableController(DayPlanService dayPlanService) {
    this.dayPlanService = dayPlanService;
  }

  @GetMapping("/groups")
  public ResponseEntity<List<String>> getGroups(
    @RequestParam(required = false) DepartmentName department,
    @RequestParam(required = false) Integer year
  ) {
    return ResponseEntity.ok(
      dayPlanService.getGroupsForClass(department, year)
    );
  }

  @GetMapping("/events")
  public ResponseEntity<List<PlanResponse>> getCalendarEvents(
    @AuthenticationPrincipal UserDetails userDetails,
    @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,
    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,
    @RequestParam(value = "department", required = false)
    DepartmentName department,
    @RequestParam(value = "year", required = false) Integer year,
    @RequestParam(value = "semester", required = false) Integer semester,
    @RequestParam(value = "group", required = false) String groupName
  ) {
    User user = (User) userDetails;
    if (user.getRole() == Role.STUDENT) {
      if (department != null && user.getDepartment() != department) {
        return ResponseEntity.ok(Collections.emptyList());
      }
      return ResponseEntity.ok(
        dayPlanService.getEventsByRange(
          startDate,
          endDate,
          user.getDepartment(),
          year != null ? year : user.getYear(),
          semester != null ? semester : user.getSemester(),
          groupName != null ? groupName : user.getGroupName()
        )
      );
    }

    return ResponseEntity.ok(
      dayPlanService.getEventsByRange(
        startDate,
        endDate,
        department,
        year,
        semester,
        groupName
      )
    );
  }

  @GetMapping("/{startDate}")
  public Map<LocalDate, List<PlanResponse>> getPlanningByRange(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,
    @RequestParam(value = "department", required = false)
    DepartmentName department,
    @RequestParam(value = "year", required = false) Integer year,
    @RequestParam(value = "semester", required = false) Integer semester,
    @RequestParam(value = "group", required = false) String groupName
  ) {
    User user = (User) userDetails;
    if (user.getRole() == Role.STUDENT) {
      if (department != null && user.getDepartment() != department) {
        return new HashMap<>();
      }
      return dayPlanService.getByWeek(
        startDate,
        user.getDepartment(),
        year != null ? year : user.getYear(),
        semester != null ? semester : user.getSemester(),
        groupName != null ? groupName : user.getGroupName()
      );
    }

    return dayPlanService.getByWeek(
      startDate,
      department,
      year,
      semester,
      groupName
    );
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<PlanResponse> createPlan(
    @RequestBody DayPlanRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    return ResponseEntity.ok(dayPlanService.createPlan(request, user));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<PlanResponse> updatePlan(
    @PathVariable String id,
    @RequestBody DayPlanRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    return ResponseEntity.ok(dayPlanService.updatePlan(id, request, user));
  }

  @PatchMapping("/{id}/schedule")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<PlanResponse> reschedulePlan(
    @PathVariable String id,
    @RequestBody DayPlanScheduleRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    return ResponseEntity.ok(dayPlanService.reschedulePlan(id, request, user));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<Void> deletePlan(
    @PathVariable String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    dayPlanService.deletePlan(id, user);
    return ResponseEntity.ok().build();
  }
}
