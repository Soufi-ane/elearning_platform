package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.UserRequest;
import com.elearn.api.service.RequestService;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

  private final RequestService requestService;

  @Autowired
  public RequestController(RequestService requestService){
    this.requestService = requestService;
  }

  @GetMapping
  public List<UserRequest> getRequests(
    @RequestParam(required = false) String studentId,
    @AuthenticationPrincipal UserDetails userDetails) 
  {
      User currentUser = (User) userDetails;

      String targetId = (currentUser.getRole() == Role.ADMIN && studentId != null) 
        ? studentId : currentUser.getId();

      return requestService.getRequestsByStudent(targetId);
  }
}

