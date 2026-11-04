package com.elearn.api.service;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.*;
import com.elearn.api.entity.Schemas.DayPlanRequest;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.repository.DayPlanRepository;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.RoomRepository;
import com.elearn.api.repository.UserRepository;

@Service
public class DayPlanService {
  private final DayPlanRepository dayPlanRepository;
  private final ElementRepository elementRepository;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  @Autowired
  public DayPlanService(
    DayPlanRepository dayPlanRepository,
    ElementRepository elementRepository,
    RoomRepository roomRepository,
    UserRepository userRepository
  ) {
    this.dayPlanRepository = dayPlanRepository;
    this.elementRepository = elementRepository;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  private Map<LocalDate, List<PlanResponse>> getWeekTemplate(LocalDate start) {
    Map<LocalDate, List<PlanResponse>> template = new TreeMap<>();
    for (int i = 0; i < 7; i++) {
      LocalDate currentDay = start.plusDays(i);
      template.put(currentDay, new ArrayList<>());
    }
    return template;
  }

  public Map<LocalDate, List<PlanResponse>> getByWeek(
    LocalDate startDate,
    DepartmentName department,
    Integer year,
    Integer semester,
    String groupName
  ) {
    if (startDate.getDayOfWeek().getValue() != 1) {
      startDate = startDate.with(ChronoField.DAY_OF_WEEK, 1);
    }
    Map<LocalDate, List<PlanResponse>> timeTable = getWeekTemplate(startDate);
    List<DayPlan> plans = dayPlanRepository.findAll();

    for (DayPlan plan : plans) {
      if (plan.getDate() == null) {
        continue;
      }
      DepartmentName planDept = plan.getDepartment() != null
        ? plan.getDepartment()
        : plan.getElement().getModule().getDepartment();
      int planYear = plan.getYear() > 0
        ? plan.getYear()
        : Math.max(1, plan.getElement().getModule().getYear());
      int planSem = plan.getSemester() > 0
        ? plan.getSemester()
        : plan.getElement().getModule().getSemester();
      String planGroup = plan.getGroupName() != null
        ? plan.getGroupName()
        : "A";

      if (department != null && planDept != department) {
        continue;
      }
      if (year != null && year > 0 && planYear != year) {
        continue;
      }
      if (semester != null && semester > 0 && planSem != semester) {
        continue;
      }
      if (
        groupName != null &&
        !groupName.isBlank() &&
        !"ALL".equalsIgnoreCase(groupName) &&
        !groupName.equalsIgnoreCase(planGroup)
      ) {
        continue;
      }

      int repeats = Math.max(1, plan.getWeeklyRepeats());
      LocalDate firstRepeat = plan.getDate();
      LocalDate lastRepeat = firstRepeat.plusWeeks(repeats - 1);

      int dayOfWeek = firstRepeat.getDayOfWeek().getValue();
      LocalDate targetDate = startDate.plusDays(dayOfWeek - 1);

      if (
        !targetDate.isBefore(firstRepeat) &&
        !targetDate.isAfter(lastRepeat) &&
        timeTable.containsKey(targetDate)
      ) {
        timeTable.get(targetDate).add(new PlanResponse(plan));
      }
    }
    return timeTable;
  }

  public List<String> getGroupsForClass(
    DepartmentName department,
    Integer year
  ) {
    Set<String> groups = new LinkedHashSet<>();
    groups.add("A");
    groups.add("B");

    userRepository.findByRole(Role.STUDENT).forEach(u -> {
      boolean deptMatch = department == null || u.getDepartment() == department;
      boolean yearMatch = year == null || year <= 0 || u.getYear() == year;
      if (deptMatch && yearMatch && u.getGroupName() != null) {
        String trimmed = u.getGroupName().trim().toUpperCase();
        if (!trimmed.isEmpty()) {
          groups.add(trimmed);
        }
      }
    });

    dayPlanRepository.findAll().forEach(p -> {
      boolean deptMatch = department == null || p.getDepartment() == department;
      boolean yearMatch = year == null || year <= 0 || p.getYear() == year;
      if (deptMatch && yearMatch && p.getGroupName() != null) {
        String trimmed = p.getGroupName().trim().toUpperCase();
        if (!trimmed.isEmpty()) {
          groups.add(trimmed);
        }
      }
    });

    return new ArrayList<>(groups);
  }

  public PlanResponse createPlan(DayPlanRequest request, User user) {
    Element element = elementRepository.findById(request.elementId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateTeacherOwnership(user, element);

    Room room = roomRepository.findById(request.roomId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    Module mod = element.getModule();
    DepartmentName dept = request.department() != null
      ? request.department()
      : mod.getDepartment();
    int year = (request.year() != null && request.year() > 0)
      ? request.year()
      : Math.max(1, mod.getYear());
    int semester = (request.semester() != null && request.semester() > 0)
      ? request.semester()
      : mod.getSemester();
    String groupName = (
      request.groupName() != null && !request.groupName().isBlank()
    )
      ? request.groupName().trim().toUpperCase()
      : "A";

    DayPlan plan = new DayPlan(
      request.date(),
      request.startsAt(),
      request.endsAt(),
      Math.max(1, request.weeklyRepeats()),
      request.type(),
      dept,
      year,
      semester,
      groupName,
      element,
      room
    );
    return new PlanResponse(dayPlanRepository.save(plan));
  }

  public PlanResponse updatePlan(
    String id,
    DayPlanRequest request,
    User user
  ) {
    DayPlan plan = dayPlanRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    Element element = elementRepository.findById(request.elementId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateTeacherOwnership(user, element);

    Room room = roomRepository.findById(request.roomId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    Module mod = element.getModule();
    DepartmentName dept = request.department() != null
      ? request.department()
      : mod.getDepartment();
    int year = (request.year() != null && request.year() > 0)
      ? request.year()
      : Math.max(1, mod.getYear());
    int semester = (request.semester() != null && request.semester() > 0)
      ? request.semester()
      : mod.getSemester();
    String groupName = (
      request.groupName() != null && !request.groupName().isBlank()
    )
      ? request.groupName().trim().toUpperCase()
      : "A";

    plan.setDate(request.date());
    plan.setStartsAt(request.startsAt());
    plan.setEndsAt(request.endsAt());
    plan.setWeeklyRepeats(Math.max(1, request.weeklyRepeats()));
    plan.setType(request.type());
    plan.setDepartment(dept);
    plan.setYear(year);
    plan.setSemester(semester);
    plan.setGroupName(groupName);
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
