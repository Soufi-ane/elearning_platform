package com.elearn.api.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.DayPlan;
import com.elearn.api.entity.Schemas.TimeTableResponse;
import com.elearn.api.repository.DayPlanRepository;

@Service 
public class DayPlanService {
  private final DayPlanRepository dayPlanRepository;

  @Autowired
  public DayPlanService(DayPlanRepository dayPlanRepository){
    this.dayPlanRepository = dayPlanRepository;
  }

  public List<TimeTableResponse> getByRange(LocalDate startDate, int rangeInDays){
    LocalDate lastDate = startDate.plusDays(rangeInDays);
    List<DayPlan> days = dayPlanRepository.findByDateBetween(startDate, lastDate);
    return days.stream()
      .map(day -> new TimeTableResponse(day))
      .toList();
  }

}
