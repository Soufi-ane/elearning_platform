package com.elearn.api.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFound(RuntimeException ex){

    Map<String, Object> body = generateBody(ex.getMessage(), HttpStatus.NOT_FOUND);
    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);

  }

  @ExceptionHandler({UsernameOrEmailTakenException.class, InvalidCredentialsException.class})
  public ResponseEntity<Map<String, Object>> handleUniqueFields(RuntimeException ex){

    Map<String, Object> body = generateBody(ex.getMessage(), HttpStatus.CONFLICT);
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);

  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequests(RuntimeException ex){

    Map<String, Object> body = generateBody(ex.getMessage(), HttpStatus.BAD_REQUEST);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);

  }

  private Map<String, Object> generateBody(String message, HttpStatus status){
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("message", message);
    body.put("status", status.value());
    return body;
  }

}
