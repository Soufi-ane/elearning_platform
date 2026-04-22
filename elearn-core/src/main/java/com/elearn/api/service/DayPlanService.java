package com.elearn.api.service;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
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

  private Map<LocalDate, List<PlanResponse>> getWeekTemplate(LocalDate start){
    Map<LocalDate,List<PlanResponse>> template = new TreeMap<>();
    for(int i=0; i<6; i++){
      LocalDate currentDay = start.plusDays(i);
      template.put(currentDay, List.of());
    }
    return template;
  }

  public Map<LocalDate, List<PlanResponse>> getByWeek(LocalDate startDate){
    if(startDate.getDayOfWeek().getValue() != 1) {
      startDate = startDate.with(ChronoField.DAY_OF_WEEK, 1);
    }
    Map<LocalDate,List<PlanResponse>> timeTable = getWeekTemplate(startDate);
    LocalDate startOfYear = LocalDate.of(startDate.getYear(),1,1);
    LocalDate endOfYear = startOfYear.plusDays(365);
    List<DayPlan> plans = dayPlanRepository.findByDateBetween(startOfYear, endOfYear);
    LocalDate currentDay = LocalDate.now();
    int dayOfWeek = plans.get(0).getDate().getDayOfWeek().getValue();
    if(!plans.isEmpty()) currentDay = startDate.with(ChronoField.DAY_OF_WEEK, dayOfWeek);
    List<PlanResponse> currentPlans = new ArrayList<>();
    for(int i=0; i < plans.size(); i++){
      int repeats = plans.get(i).getWeeklyRepeats();
      LocalDate firstRepeat = plans.get(i).getDate();
      LocalDate lastRepeat = firstRepeat.plusDays(7 * repeats);
      if(startDate.plusDays(6).isBefore(firstRepeat) || startDate.isAfter(lastRepeat)){
        continue;
      }
      if(dayOfWeek != plans.get(i).getDate().getDayOfWeek().getValue()) {
        timeTable.put(currentDay, currentPlans);
        dayOfWeek = plans.get(i).getDate().getDayOfWeek().getValue();
        currentDay = startDate.with(ChronoField.DAY_OF_WEEK, dayOfWeek);
        currentPlans = new ArrayList<>();
      }
      currentPlans.add(new PlanResponse(plans.get(i)));
      if(i == plans.size() - 1) timeTable.put(currentDay, currentPlans);

    }
    return timeTable;
  }

}
