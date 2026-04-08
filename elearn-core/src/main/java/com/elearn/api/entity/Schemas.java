package com.elearn.api.entity;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class Schemas {

  public record DepartmentBaseResponse(String id, String name){}

  public record RegisterRequest(
    String firstName, String lastName, 
    String username, String email, String password, 
    @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dateOfBirth,
    Role role, String departmentId
  ){}

  public record UserBaseResponse(
    String firstName, String lastName, 
    String username, String email,
    LocalDate dateOfBirth, Role role,
    String departmentId
  ){
    public UserBaseResponse(User user){
      this(
        user.getFirstName(), user.getLastName(), user.getDbUsername(),
        user.getUsername(), user.getDateOfBirth(), user.getRole(),
        user.getDepartment() == null ? null : user.getDepartment().getId()
      );
    }
  }

  public record LoginRequest(String usernameOrEmail,String password){}

}
