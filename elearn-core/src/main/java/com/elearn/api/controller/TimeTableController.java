package com.elearn.api.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.Schemas.TimeTableResponse;
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
  public List<TimeTableResponse> getPlanningByRange(
    @PathVariable("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(value = "range") int rangeInDays
  ){
    return dayPlanService.getByRange(startDate,rangeInDays);
  }

}
