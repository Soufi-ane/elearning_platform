package com.elearn.api.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.DayPlan;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.repository.DayPlanRepository;

@Service 
public class DayPlanService {
  private final DayPlanRepository dayPlanRepository;

  @Autowired
  public DayPlanService(DayPlanRepository dayPlanRepository){
    this.dayPlanRepository = dayPlanRepository;
  }

  public Map<LocalDate, List<PlanResponse>> getByWeek(LocalDate startDate){
    Map<LocalDate,List<PlanResponse>> timeTable = new TreeMap<>();
    List<DayPlan> plans = dayPlanRepository.findByDateBetweenOrderByDateAsc(startDate, startDate.plusDays(7));
    LocalDate currentDay = LocalDate.now();
    if(!plans.isEmpty()) currentDay = plans.get(0).getDate();
    List<PlanResponse> currentPlans = new ArrayList<>();
    for(int i=0; i < plans.size(); i++){
      if(!plans.get(i).getDate().isEqual(currentDay)) {
        timeTable.put(currentDay, currentPlans);
        currentDay = plans.get(i).getDate();
        currentPlans = new ArrayList<>();
      }
      currentPlans.add(new PlanResponse(plans.get(i)));
      if(i == plans.size() - 1) timeTable.put(currentDay, currentPlans);
    }
    return timeTable;
  }

}
