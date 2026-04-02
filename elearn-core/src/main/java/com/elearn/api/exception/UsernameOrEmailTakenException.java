package com.elearn.api.exception;

public class UsernameOrEmailTakenException extends RuntimeException {
  public UsernameOrEmailTakenException(String message){
    super(message);
  }
}
