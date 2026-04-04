package com.elearn.api.exception;

public class InvalidCredentialsException extends RuntimeException {
  public InvalidCredentialsException(String message){
    super(message);
  }
}
