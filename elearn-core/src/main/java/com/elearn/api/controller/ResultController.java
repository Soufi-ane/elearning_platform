package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Schemas.ElementGradeStatsResponse;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ResultCreateRequest;
import com.elearn.api.entity.Schemas.ResultResponse;
import com.elearn.api.entity.Schemas.ResultUpdateRequest;
import com.elearn.api.entity.Schemas.SemesterSummaryResponse;
import com.elearn.api.entity.Schemas.UserBaseResponse;
import com.elearn.api.entity.User;
import com.elearn.api.service.ResultService;

@RestController
@RequestMapping("/api/v1/results")
public class ResultController {
  private final ResultService resultService;

  @Autowired
  public ResultController(ResultService resultService) {
    this.resultService = resultService;
  }

  @GetMapping("/{studentId}/{semester}")
  public List<ResultResponse> listStudentResultsBySemester(
    @PathVariable("studentId") String studentId,
    @PathVariable("semester") int semester,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.listBySemester(semester, studentId, (User) userDetails);
  }

  @GetMapping("/summary/{studentId}/{semester}")
  public SemesterSummaryResponse getSemesterSummary(
    @PathVariable("studentId") String studentId,
    @PathVariable("semester") int semester,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.getStudentSemesterSummary(
      semester,
      studentId,
      (User) userDetails
    );
  }

  @GetMapping("/manage")
  public List<ResultResponse> searchGrades(
    @RequestParam(required = false) DepartmentName department,
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer semester,
    @RequestParam(required = false) String groupName,
    @RequestParam(required = false) String elementId,
    @RequestParam(required = false) String teacherId,
    @RequestParam(required = false) String query,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.searchGrades(
      (User) userDetails,
      department,
      year,
      semester,
      groupName,
      elementId,
      teacherId,
      query
    );
  }

  @GetMapping("/elements")
  public List<ElementResponse> listControllableElements(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.listControllableElements((User) userDetails);
  }

  @GetMapping("/students")
  public List<UserBaseResponse> listStudentsForGrading(
    @RequestParam(required = false) DepartmentName department,
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) String groupName,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.listStudentsForGrading(
      (User) userDetails,
      department,
      year,
      groupName
    );
  }

  @GetMapping("/stats/{elementId}")
  public ElementGradeStatsResponse getElementStats(
    @PathVariable("elementId") String elementId,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.getElementStats(elementId, (User) userDetails);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResultResponse createResult(
    @RequestBody ResultCreateRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.createResult(request, (User) userDetails);
  }

  @PutMapping("/{id}")
  public ResultResponse updateResult(
    @PathVariable("id") String id,
    @RequestBody ResultUpdateRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return resultService.updateResult(id, request, (User) userDetails);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteResult(
    @PathVariable("id") String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    resultService.deleteResult(id, (User) userDetails);
  }
}
