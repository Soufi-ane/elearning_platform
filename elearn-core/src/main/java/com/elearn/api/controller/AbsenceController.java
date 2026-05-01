package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.AbsenceByElementResponse;
import com.elearn.api.entity.Schemas.AbsenceResponse;
import com.elearn.api.service.AbsenceService;

@RestController
@RequestMapping("/api/v1/absence")
public class AbsenceController {
  private final AbsenceService absenceService;

  @Autowired
  public AbsenceController(AbsenceService absenceService){
    this.absenceService = absenceService;
  }

  @GetMapping
  public List<AbsenceByElementResponse> findAll(
    @AuthenticationPrincipal UserDetails userDetails,
    @RequestParam(required = false) String userId
  ){
    User user = (User) userDetails;
    String id = user.getRole() == Role.STUDENT ? user.getId() : userId;
    return absenceService.listByUser(id);
  }

  @GetMapping("/{elementId}")
  public List<AbsenceResponse> getByElementId(
    @PathVariable String elementId,
    @RequestParam(required = false) String userId,
    @AuthenticationPrincipal UserDetails userDetails
  ){
    User user = (User) userDetails;
    String targetUserId = user.getRole() == Role.STUDENT ? user.getId() : userId;
    return absenceService.getByElementId(elementId, targetUserId);
  }

}
