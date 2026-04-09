package com.elearn.api.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Schemas {

  public record DepartmentBaseResponse(String id, String name){}

  public record RegisterRequest(
    String firstName, String lastName, 
    String username, String email, String password, 
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
    Role role, String departmentId
  ){}

  public record UserBaseResponse(String id, String firstName, String lastName){}

  public record UserResponse(
    String id, String firstName, String lastName, 
    String username, String email,
    LocalDate dateOfBirth, Role role,
    String departmentId
  ){
    public UserResponse(User user){
      this(
        user.getId(), user.getFirstName(), user.getLastName(), user.getDbUsername(),
        user.getUsername(), user.getDateOfBirth(), user.getRole(),
        user.getDepartment() == null ? null : user.getDepartment().getId()
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

  public record TimeTableResponse(
    String id, LocalDate date, LocalTime startsAt, LocalTime endsAt,
    int weeklyRepeats, PlanType type, ElementResponse element
  ){
    public TimeTableResponse(DayPlan plan){
      this(
        plan.getId(), plan.getDate(), plan.getStartsAt(),
        plan.getEndsAt(), plan.getWeeklyRepeats(), plan.getType(),
        new ElementResponse(plan.getElement())
      );
    }
  }

}
