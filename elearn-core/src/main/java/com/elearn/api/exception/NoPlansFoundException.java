package com.elearn.api.exception;

public class NoPlansFoundException extends RuntimeException {
  public NoPlansFoundException(String message){
    super(message);
  }
}
