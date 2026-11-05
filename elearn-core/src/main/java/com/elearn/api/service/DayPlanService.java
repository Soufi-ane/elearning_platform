package com.elearn.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.DayPlan;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Module;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.Room;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.DayPlanRequest;
import com.elearn.api.entity.Schemas.DayPlanScheduleRequest;
import com.elearn.api.entity.Schemas.PlanResponse;
import com.elearn.api.exception.BadRequestException;
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

  public List<PlanResponse> getEventsByRange(
    LocalDate startDate,
    LocalDate endDate,
    DepartmentName department,
    Integer year,
    Integer semester,
    String groupName
  ) {
    List<PlanResponse> events = new ArrayList<>();
    List<DayPlan> plans = dayPlanRepository.findAll();

    for (DayPlan plan : plans) {
      if (!matchesFilter(plan, department, year, semester, groupName)) {
        continue;
      }

      int repeats = Math.max(1, plan.getWeeklyRepeats());
      LocalDate baseDate = plan.getDate();

      for (int week = 0; week < repeats; week++) {
        LocalDate occurrence = baseDate.plusWeeks(week);
        if (
          !occurrence.isBefore(startDate) &&
          !occurrence.isAfter(endDate)
        ) {
          events.add(new PlanResponse(plan, occurrence));
        }
      }
    }

    events.sort(
      Comparator.comparing(PlanResponse::date)
        .thenComparing(PlanResponse::startsAt)
    );
    return events;
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

    LocalDate endDate = startDate.plusDays(6);
    Map<LocalDate, List<PlanResponse>> timeTable = getWeekTemplate(startDate);
    List<PlanResponse> weekEvents = getEventsByRange(
      startDate,
      endDate,
      department,
      year,
      semester,
      groupName
    );

    for (PlanResponse event : weekEvents) {
      if (timeTable.containsKey(event.date())) {
        timeTable.get(event.date()).add(event);
      }
    }

    return timeTable;
  }

  private boolean matchesFilter(
    DayPlan plan,
    DepartmentName department,
    Integer year,
    Integer semester,
    String groupName
  ) {
    if (plan.getDate() == null || plan.getElement() == null) {
      return false;
    }

    Module mod = plan.getElement().getModule();
    DepartmentName planDept = plan.getDepartment() != null
      ? plan.getDepartment()
      : mod.getDepartment();
    int planYear = plan.getYear() > 0
      ? plan.getYear()
      : Math.max(1, mod.getYear());
    int planSem = plan.getSemester() > 0
      ? plan.getSemester()
      : mod.getSemester();
    String planGroup = plan.getGroupName() != null
      ? plan.getGroupName()
      : "A";

    if (department != null && planDept != department) {
      return false;
    }
    if (year != null && year > 0 && planYear != year) {
      return false;
    }
    if (semester != null && semester > 0 && planSem != semester) {
      return false;
    }
    if (
      groupName != null &&
      !groupName.isBlank() &&
      !"ALL".equalsIgnoreCase(groupName) &&
      !groupName.equalsIgnoreCase(planGroup)
    ) {
      return false;
    }
    return true;
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
    int repeats = Math.max(1, request.weeklyRepeats());

    validateScheduleAndConflicts(
      null,
      request.date(),
      request.startsAt(),
      request.endsAt(),
      repeats,
      room,
      element
    );

    DayPlan plan = new DayPlan(
      request.date(),
      request.startsAt(),
      request.endsAt(),
      repeats,
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
    int repeats = Math.max(1, request.weeklyRepeats());

    validateScheduleAndConflicts(
      id,
      request.date(),
      request.startsAt(),
      request.endsAt(),
      repeats,
      room,
      element
    );

    plan.setDate(request.date());
    plan.setStartsAt(request.startsAt());
    plan.setEndsAt(request.endsAt());
    plan.setWeeklyRepeats(repeats);
    plan.setType(request.type());
    plan.setDepartment(dept);
    plan.setYear(year);
    plan.setSemester(semester);
    plan.setGroupName(groupName);
    plan.setElement(element);
    plan.setRoom(room);

    return new PlanResponse(dayPlanRepository.save(plan));
  }

  public PlanResponse reschedulePlan(
    String id,
    DayPlanScheduleRequest request,
    User user
  ) {
    DayPlan plan = dayPlanRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    validateTeacherOwnership(user, plan.getElement());

    Room targetRoom = plan.getRoom();
    if (request.roomId() != null && !request.roomId().isBlank()) {
      targetRoom = roomRepository.findById(request.roomId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    LocalDate newDate = request.date() != null
      ? request.date()
      : plan.getDate();
    LocalTime newStart = request.startsAt() != null
      ? request.startsAt()
      : plan.getStartsAt();
    LocalTime newEnd = request.endsAt() != null
      ? request.endsAt()
      : plan.getEndsAt();

    validateScheduleAndConflicts(
      id,
      newDate,
      newStart,
      newEnd,
      Math.max(1, plan.getWeeklyRepeats()),
      targetRoom,
      plan.getElement()
    );

    plan.setDate(newDate);
    plan.setStartsAt(newStart);
    plan.setEndsAt(newEnd);
    plan.setRoom(targetRoom);

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

  private void validateScheduleAndConflicts(
    String excludePlanId,
    LocalDate date,
    LocalTime startsAt,
    LocalTime endsAt,
    int weeklyRepeats,
    Room room,
    Element element
  ) {
    if (date == null || startsAt == null || endsAt == null) {
      throw new BadRequestException("Date, start time, and end time required");
    }
    if (!endsAt.isAfter(startsAt)) {
      throw new BadRequestException("End time must be after start time");
    }

    LocalDate lastDate = date.plusWeeks(Math.max(1, weeklyRepeats) - 1);

    for (DayPlan existing : dayPlanRepository.findAll()) {
      if (excludePlanId != null && excludePlanId.equals(existing.getId())) {
        continue;
      }
      if (existing.getDate() == null || existing.getStartsAt() == null) {
        continue;
      }
      if (existing.getDate().getDayOfWeek() != date.getDayOfWeek()) {
        continue;
      }

      LocalDate existingEnd = existing.getDate().plusWeeks(
        Math.max(1, existing.getWeeklyRepeats()) - 1
      );
      boolean weeksOverlap = !date.isAfter(existingEnd) &&
        !existing.getDate().isAfter(lastDate);
      boolean timesOverlap = startsAt.isBefore(existing.getEndsAt()) &&
        endsAt.isAfter(existing.getStartsAt());

      if (weeksOverlap && timesOverlap) {
        if (
          existing.getRoom() != null &&
          existing.getRoom().getId().equals(room.getId())
        ) {
          throw new BadRequestException(
            "Room " + room.getLabel() + " is already booked during this slot"
          );
        }
        if (
          existing.getElement() != null &&
          existing.getElement().getTeacher() != null &&
          element.getTeacher() != null &&
          existing.getElement().getTeacher().getId().equals(
            element.getTeacher().getId()
          )
        ) {
          throw new BadRequestException(
            "Instructor is already scheduled in another session at this time"
          );
        }
      }
    }
  }
}
