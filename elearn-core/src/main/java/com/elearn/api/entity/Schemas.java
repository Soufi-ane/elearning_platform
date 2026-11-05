package com.elearn.api.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Schemas {
  public record RegisterRequest(
    String firstName,
    String lastName,
    String username,
    String email,
    String password,
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
    Role role,
    DepartmentName department,
    StudyMode studyMode,
    int year,
    int semester,
    String groupName
  ) {}

  public record UserBaseResponse(
    String id,
    String firstName,
    String lastName
  ) {}

  public record UserResponse(
    String id,
    String firstName,
    String lastName,
    String username,
    String email,
    LocalDate dateOfBirth,
    Role role,
    DepartmentName department,
    StudyMode studyMode,
    int year,
    int semester,
    int currentSemester,
    int numSemesters,
    String groupName
  ) {
    public UserResponse(User user) {
      this(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getDbUsername(),
        user.getUsername(),
        user.getDateOfBirth(),
        user.getRole(),
        user.getDepartment(),
        user.getStudyMode(),
        user.getYear(),
        user.getSemester(),
        user.getSemester(),
        user.getNumSemesters(),
        user.getGroupName()
      );
    }
  }

  public record LoginRequest(String usernameOrEmail, String password) {}

  public record ModuleBaseResponse(
    String id,
    String name,
    int year,
    int semester,
    DepartmentName department
  ) {
    public ModuleBaseResponse(Module module) {
      this(
        module.getId(),
        module.getName(),
        module.getYear(),
        module.getSemester(),
        module.getDepartment()
      );
    }
  }

  public record ElementResponse(
    String id,
    String name,
    ModuleBaseResponse module,
    UserBaseResponse teacher
  ) {
    public ElementResponse(Element element) {
      this(
        element.getId(),
        element.getName(),
        new ModuleBaseResponse(element.getModule()),
        new UserBaseResponse(
          element.getTeacher().getId(),
          element.getTeacher().getFirstName(),
          element.getTeacher().getLastName()
        )
      );
    }
  }

  public record RoomBaseResponse(
    String id,
    String label,
    int floor,
    String campus
  ) {
    public RoomBaseResponse(Room room) {
      this(
        room.getId(),
        room.getLabel(),
        room.getFloor(),
        room.getCampus().getName()
      );
    }
  }

  public record PlanResponse(
    String id,
    LocalDate date,
    LocalTime startsAt,
    LocalTime endsAt,
    int weeklyRepeats,
    PlanType type,
    DepartmentName department,
    int year,
    int semester,
    String groupName,
    ElementResponse element,
    RoomBaseResponse room
  ) {
    public PlanResponse(DayPlan plan) {
      this(plan, plan.getDate());
    }

    public PlanResponse(DayPlan plan, LocalDate occurrenceDate) {
      this(
        plan.getId(),
        occurrenceDate,
        plan.getStartsAt(),
        plan.getEndsAt(),
        plan.getWeeklyRepeats(),
        plan.getType(),
        plan.getDepartment(),
        plan.getYear(),
        plan.getSemester(),
        plan.getGroupName(),
        new ElementResponse(plan.getElement()),
        new RoomBaseResponse(plan.getRoom())
      );
    }
  }

  public record DayPlanScheduleRequest(
    LocalDate date,
    LocalTime startsAt,
    LocalTime endsAt,
    String roomId
  ) {}

  public record AbsenceByElementResponse(
    ElementResponse element,
    int count,
    AbsenceStatus status
  ) {}

  public record AbsenceResponse(
    String id,
    LocalDateTime dateTime,
    AbsenceType type,
    boolean isJustified,
    UserBaseResponse student,
    ElementResponse element
  ) {
    public AbsenceResponse(Absence a) {
      this(
        a.getId(),
        a.getDateTime(),
        a.getType(),
        a.isJustified(),
        new UserBaseResponse(
          a.getStudent().getId(),
          a.getStudent().getFirstName(),
          a.getStudent().getLastName()
        ),
        new ElementResponse(a.getElement())
      );
    }
  }

  public record ResultResponse(
    String id,
    double grade,
    UserBaseResponse student,
    ElementResponse element
  ) {
    public ResultResponse(Result r) {
      this(
        r.getId(),
        r.getGrade(),
        new UserBaseResponse(
          r.getStudent().getId(),
          r.getStudent().getFirstName(),
          r.getStudent().getLastName()
        ),
        new ElementResponse(r.getElement())
      );
    }
  }

  public record UserRequest(
    String id,
    String title,
    String state,
    Date requestDate,
    Date forcastDate,
    UserBaseResponse student
  ) {
    public UserRequest(Request r) {
      this(
        r.getId(),
        r.getTitle(),
        r.getState() != null ? r.getState().name() : null,
        r.getRequestDate(),
        r.getForcastDate(),
        new UserBaseResponse(
          r.getStudent().getId(),
          r.getStudent().getFirstName(),
          r.getStudent().getLastName()
        )
      );
    }
  }

  public record ModuleResponse(
    String id,
    String name,
    int year,
    int semester,
    DepartmentName department
  ) {
    public ModuleResponse(Module m) {
      this(
        m.getId(),
        m.getName(),
        m.getYear(),
        m.getSemester(),
        m.getDepartment()
      );
    }
  }

  public record AbsenceCreateRequest(
    String studentId,
    String elementId,
    LocalDateTime dateTime,
    AbsenceType type
  ) {
    public record AbsenceUpdateRequest(
      Boolean isJustified,
      AbsenceType type
    ) {}
  }

  public record AbsenceAdminResponse(
    String id,
    LocalDateTime dateTime,
    AbsenceType type,
    boolean isJustified,
    UserBaseResponse student,
    ElementResponse element
  ) {
    public AbsenceAdminResponse(Absence a) {
      this(
        a.getId(),
        a.getDateTime(),
        a.getType(),
        a.isJustified(),
        new UserBaseResponse(
          a.getStudent().getId(),
          a.getStudent().getFirstName(),
          a.getStudent().getLastName()
        ),
        new ElementResponse(a.getElement())
      );
    }
  }

  public record ModuleRequest(
    String name,
    Integer year,
    int semester,
    DepartmentName department
  ) {}

  public record ElementRequest(
    String name,
    String moduleId,
    String teacherId
  ) {}

  public record DayPlanRequest(
    LocalDate date,
    LocalTime startsAt,
    LocalTime endsAt,
    int weeklyRepeats,
    PlanType type,
    DepartmentName department,
    Integer year,
    Integer semester,
    String groupName,
    String elementId,
    String roomId
  ) {}
}
