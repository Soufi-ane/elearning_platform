package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.Schemas.ResultResponse;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.service.ResultService;

@RestController
@RequestMapping("/api/v1/reuslts")
public class ResultController {
  private final ResultService resultService;

  @Autowired
  public ResultController(ResultService resultService){
    this.resultService = resultService;
  }

  @GetMapping("/{studentId}/{semester}")
  public List<ResultResponse> listStudentResultsBySemester(
      @PathVariable("studentId") String studentId,
      @PathVariable("semester") int semester,
      @AuthenticationPrincipal UserDetails userDetails
    ){
    User user = (User) userDetails;
    boolean isNotStudent = user.getRole() != Role.STUDENT;
    return resultService.listBySemester(semester, isNotStudent ? studentId : user.getId());
  }

}
