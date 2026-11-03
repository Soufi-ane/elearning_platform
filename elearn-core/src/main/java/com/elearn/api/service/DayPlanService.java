package com.elearn.api.service;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.DayPlan;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.Room;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.DayPlanRequest;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.repository.DayPlanRepository;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.RoomRepository;

@Service 
public class DayPlanService {
  private final DayPlanRepository dayPlanRepository;
  private final ElementRepository elementRepository;
  private final RoomRepository roomRepository;

  @Autowired
  public DayPlanService(
    DayPlanRepository dayPlanRepository,
    ElementRepository elementRepository,
    RoomRepository roomRepository
  ) {
    this.dayPlanRepository = dayPlanRepository;
    this.elementRepository = elementRepository;
    this.roomRepository = roomRepository;
  }

  private Map<LocalDate, List<PlanResponse>> getWeekTemplate(LocalDate start) {
    Map<LocalDate,List<PlanResponse>> template = new TreeMap<>();
    for(int i=0; i<7; i++) {
      LocalDate currentDay = start.plusDays(i);
      template.put(currentDay, new ArrayList<>());
    }
    return template;
  }

  public Map<LocalDate, List<PlanResponse>> getByWeek(LocalDate startDate) {
    if(startDate.getDayOfWeek().getValue() != 1) {
      startDate = startDate.with(ChronoField.DAY_OF_WEEK, 1);
    }
    Map<LocalDate,List<PlanResponse>> timeTable = getWeekTemplate(startDate);
    
    LocalDate startOfYear = LocalDate.of(startDate.getYear(), 1, 1);
    LocalDate endOfYear = startOfYear.plusDays(365);
    List<DayPlan> plans = dayPlanRepository.findByDateBetween(startOfYear, endOfYear);

    for (DayPlan plan : plans) {
      int repeats = plan.getWeeklyRepeats();
      LocalDate firstRepeat = plan.getDate();
      LocalDate lastRepeat = firstRepeat.plusDays(7L * repeats);

      if(startDate.plusDays(6).isBefore(firstRepeat) || startDate.isAfter(lastRepeat)) {
        continue;
      }

      int dayOfWeek = plan.getDate().getDayOfWeek().getValue();
      LocalDate targetDate = startDate.plusDays(dayOfWeek - 1);

      if (timeTable.containsKey(targetDate)) {
        timeTable.get(targetDate).add(new PlanResponse(plan));
      }
    }
    return timeTable;
  }

  public PlanResponse createPlan(DayPlanRequest request, User user) {
    Element element = elementRepository.findById(request.elementId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    
    validateTeacherOwnership(user, element);

    Room room = roomRepository.findById(request.roomId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    DayPlan plan = new DayPlan(
      request.date(), request.startsAt(), request.endsAt(), 
      request.weeklyRepeats(), request.type(), element, room
    );
    return new PlanResponse(dayPlanRepository.save(plan));
  }

  public PlanResponse updatePlan(String id, DayPlanRequest request, User user) {
    DayPlan plan = dayPlanRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    
    Element element = elementRepository.findById(request.elementId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateTeacherOwnership(user, element);

    Room room = roomRepository.findById(request.roomId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    plan.setDate(request.date());
    plan.setStartsAt(request.startsAt());
    plan.setEndsAt(request.endsAt());
    plan.setWeeklyRepeats(request.weeklyRepeats());
    plan.setType(request.type());
    plan.setElement(element);
    plan.setRoom(room);

    return new PlanResponse(dayPlanRepository.save(plan));
  }

  public void deletePlan(String id, User user) {
    DayPlan plan = dayPlanRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    
    validateTeacherOwnership(user, plan.getElement());
    dayPlanRepository.deleteById(id);
  }

  private void validateTeacherOwnership(User user, Element element) {
    if (user.getRole() == Role.TEACHER) {
      if (!element.getTeacher().getId().equals(user.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
    }
  }
}
