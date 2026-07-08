package com.elearn.api.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Schemas {

  public record RegisterRequest(
    String firstName, String lastName, 
    String username, String email, String password, 
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
    Role role, DepartmentName department, StudyMode studyMode, int year, int semester
  ){}

  public record UserBaseResponse(String id, String firstName, String lastName){}

  public record UserResponse(
    String id, String firstName, String lastName, 
    String username, String email, LocalDate dateOfBirth,
    Role role, DepartmentName department,
    StudyMode studyMode, int year, int semester, int currentSemester
  ){
    public UserResponse(User user){
      this(
        user.getId(), user.getFirstName(), user.getLastName(), user.getDbUsername(),
        user.getUsername(), user.getDateOfBirth(), user.getRole(),
        user.getDepartment() == null ? null : user.getDepartment(),
        user.getStudyMode(), user.getYear(), user.getSemester(), user.getNumSemesters()
      );
    }
  }

  public record LoginRequest(String usernameOrEmail,String password){}

  public record ModuleBaseResponse(String id, String name){
    public ModuleBaseResponse(Module module){
      this(module.getId(), module.getName());
    }
  }

  public record ElementResponse(
    String id, String name,ModuleBaseResponse module, UserBaseResponse teacher
  ){
    public ElementResponse(Element element){
      this(
        element.getId(), element.getName(),
        new ModuleBaseResponse(element.getModule()),
        new UserBaseResponse(
          element.getTeacher().getId(),
          element.getTeacher().getFirstName(),
          element.getTeacher().getLastName()
        )
      );
    }
  }

  public record RoomBaseResponse(String label,int floor, String campus){
    public RoomBaseResponse(Room room){
      this(
        room.getLabel(), room.getFloor(),
        room.getCampus().getName()
      );
    }
  }

  public record PlanResponse(
    LocalTime startsAt, LocalTime endsAt, PlanType type,
    ElementResponse element, RoomBaseResponse room
  ){
    public PlanResponse(DayPlan plan){
      this(
        plan.getStartsAt(), plan.getEndsAt(), plan.getType(),
        new ElementResponse(plan.getElement()), new RoomBaseResponse(plan.getRoom())
      );
    }
  }

  public record AbsenceByElementResponse(
    ElementResponse element, int count, AbsenceStatus status
  ){}

  public record AbsenceResponse( 
    String id, LocalDateTime dateTime, AbsenceType type, boolean isJustified,
    UserBaseResponse student,ElementResponse element
  ){
    public AbsenceResponse(Absence a) {
      this(
        a.getId(), a.getDateTime(), a.getType(), a.isJustified(),
        new UserBaseResponse(
          a.getStudent().getId(), a.getStudent().getFirstName(),
          a.getStudent().getLastName()
        ),
        new ElementResponse(a.getElement())
      );
    }
  }

  public record ResultResponse(
    String id, double grade, UserBaseResponse student, ElementResponse element
  ){
    public ResultResponse(Result r){
      this(
        r.getId(), r.getGrade(), 
        new UserBaseResponse(
          r.getStudent().getId(),
          r.getStudent().getFirstName(),
          r.getStudent().getLastName()
        ),
        new ElementResponse(r.getElement())
      );
    }
  }

}
