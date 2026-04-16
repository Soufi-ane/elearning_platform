package com.elearn.api.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.service.DayPlanService;

@RestController
@RequestMapping("/api/v1/timeTable")
public class TimeTableController {

  private final DayPlanService dayPlanService;

  @Autowired
  public TimeTableController(DayPlanService dayPlanService){
    this.dayPlanService = dayPlanService;
  }

  @GetMapping("/{startDate}")
  public Map<LocalDate, List<PlanResponse>> getPlanningByRange(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(value = "departmentId") String departmentId
  ){

    User user = (User) userDetails;
    boolean isAdmin = user.getRole() == Role.ADMIN;
    boolean isDepartmentValid = false;
    if(!isAdmin) isDepartmentValid = user.getDepartment().getId().equals(departmentId);
    if(!isAdmin && !isDepartmentValid) return new HashMap<>();

    return dayPlanService.getByWeek(startDate);
  }

}
