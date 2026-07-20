package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.AbsenceByElementResponse;
import com.elearn.api.entity.Schemas.AbsenceCreateRequest;
import com.elearn.api.entity.Schemas.AbsenceResponse;
import com.elearn.api.entity.Schemas.AbsenceCreateRequest.AbsenceUpdateRequest;
import com.elearn.api.service.AbsenceService;

@RestController
@RequestMapping("/api/v1/absence")
public class AbsenceController {
  private final AbsenceService absenceService;

  @Autowired
  public AbsenceController(AbsenceService absenceService){
    this.absenceService = absenceService;
  }

  /* @GetMapping("/department/{deptName}")
    public List<AbsenceByElementResponse> getByDepartment(
      @PathVariable String deptName,
      @AuthenticationPrincipal UserDetails userDetails
    ){
     return absenceService.listByDepartmen(deptName);
  } */

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

  @PostMapping
  public AbsenceResponse createAbsence(
    @RequestBody AbsenceCreateRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ){
    User user = (User) userDetails;
    if (user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to create absences.");
    }
    return absenceService.createAbsence(request);
  }

  @PutMapping("/{absenceId}")
  public AbsenceResponse updateAbsence(
    @PathVariable String absenceId,
    @RequestBody AbsenceUpdateRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ){
    return absenceService.updateAbsence(absenceId, request);
  }

  @GetMapping("/manage")
  public List<AbsenceResponse> getManagedAbsences(
    @AuthenticationPrincipal UserDetails userDetails,
    @RequestParam(required = false) String department,
    @RequestParam(required = false) String searchName
  ) {
    User user = (User) userDetails;

    if (user.getRole() == Role.ADMIN) {
      return absenceService.getAdminAbsences(department, searchName);
    } 
    else if (user.getRole() == Role.TEACHER) {
      return absenceService.getTeacherAbsences(user.getId());
    } 
    else {
      throw new RuntimeException("Unauthorized access");
    }
  }
  @DeleteMapping("/{absenceId}")
  public ResponseEntity<?> deleteAbsence(
    @PathVariable String absenceId,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    User user = (User) userDetails;
    if (user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete absences.");
    }
    absenceService.deleteAbsence(absenceId);
    return ResponseEntity.ok().build();
  }

}
