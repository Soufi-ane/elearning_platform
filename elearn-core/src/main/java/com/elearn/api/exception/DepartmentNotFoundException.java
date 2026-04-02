package com.elearn.api.exception;

public class DepartmentNotFoundException extends RuntimeException {
  public DepartmentNotFoundException(String message){
    super(message);
  }
}
