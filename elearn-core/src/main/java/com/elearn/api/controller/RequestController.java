package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.RequestState;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.RequestCreateDto;
import com.elearn.api.entity.Schemas.RequestUpdateDto;
import com.elearn.api.entity.Schemas.UserRequest;
import com.elearn.api.service.RequestService;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {
  private final RequestService requestService;

  @Autowired
  public RequestController(RequestService requestService) {
    this.requestService = requestService;
  }

  @GetMapping
  public List<UserRequest> getRequests(
    @RequestParam(required = false) String studentId,
    @RequestParam(required = false) RequestState state,
    @RequestParam(required = false) Role role,
    @RequestParam(required = false) String search,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User currentUser = (User) userDetails;
    return requestService.getRequests(
      currentUser,
      studentId,
      state,
      role,
      search
    );
  }

  @PostMapping
  public ResponseEntity<UserRequest> createRequest(
    @RequestBody RequestCreateDto dto,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User currentUser = (User) userDetails;
    return ResponseEntity.ok(
      requestService.createRequest(dto, currentUser)
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserRequest> updateRequest(
    @PathVariable String id,
    @RequestBody RequestUpdateDto dto,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User currentUser = (User) userDetails;
    return ResponseEntity.ok(
      requestService.updateRequest(id, dto, currentUser)
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRequest(
    @PathVariable String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User currentUser = (User) userDetails;
    requestService.deleteRequest(id, currentUser);
    return ResponseEntity.ok().build();
  }
}
