package com.elearn.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Module;
import com.elearn.api.entity.Result;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.Schemas.ElementGradeStatsResponse;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleAverageResponse;
import com.elearn.api.entity.Schemas.ModuleBaseResponse;
import com.elearn.api.entity.Schemas.ResultCreateRequest;
import com.elearn.api.entity.Schemas.ResultResponse;
import com.elearn.api.entity.Schemas.ResultUpdateRequest;
import com.elearn.api.entity.Schemas.SemesterSummaryResponse;
import com.elearn.api.entity.Schemas.UserBaseResponse;
import com.elearn.api.entity.User;
import com.elearn.api.exception.BadRequestException;
import com.elearn.api.exception.ResourceNotFoundException;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.ResultRepository;
import com.elearn.api.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

@Service
public class ResultService {
  private final ResultRepository resultRepository;
  private final ElementRepository elementRepository;
  private final UserRepository userRepository;

  @Autowired
  public ResultService(
    ResultRepository resultRepository,
    ElementRepository elementRepository,
    UserRepository userRepository
  ) {
    this.resultRepository = resultRepository;
    this.elementRepository = elementRepository;
    this.userRepository = userRepository;
  }

  public List<ResultResponse> listBySemester(
    int semester,
    String targetStudentId,
    User actor
  ) {
    String studentId = actor.getRole() == Role.STUDENT
      ? actor.getId()
      : targetStudentId;
    List<Result> results = resultRepository
      .findByElement_Module_SemesterAndStudent_Id(semester, studentId);

    if (actor.getRole() == Role.TEACHER) {
      results = results.stream()
        .filter(r -> isTeacherOwner(actor, r.getElement()))
        .toList();
    }

    return results.stream().map(ResultResponse::new).toList();
  }

  public SemesterSummaryResponse getStudentSemesterSummary(
    int semester,
    String targetStudentId,
    User actor
  ) {
    String studentId = actor.getRole() == Role.STUDENT
      ? actor.getId()
      : targetStudentId;
    List<Result> results = resultRepository
      .findByElement_Module_SemesterAndStudent_Id(semester, studentId);

    if (actor.getRole() == Role.TEACHER) {
      results = results.stream()
        .filter(r -> isTeacherOwner(actor, r.getElement()))
        .toList();
    }

    return buildSemesterSummary(semester, results);
  }

  public List<ResultResponse> searchGrades(
    User actor,
    DepartmentName department,
    Integer year,
    Integer semester,
    String groupName,
    String elementId,
    String teacherId,
    String query
  ) {
    requireStaff(actor);

    Specification<Result> spec = (root, cq, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      Join<Result, User> student = root.join("student");
      Join<Result, Element> element = root.join("element");
      Join<Element, Module> module = element.join("module");

      if (actor.getRole() == Role.TEACHER) {
        predicates.add(
          cb.equal(element.get("teacher").get("id"), actor.getId())
        );
      } else if (teacherId != null && !teacherId.isBlank()) {
        predicates.add(
          cb.equal(element.get("teacher").get("id"), teacherId)
        );
      }

      if (department != null) {
        predicates.add(cb.equal(module.get("department"), department));
      }
      if (year != null) {
        predicates.add(cb.equal(module.get("year"), year));
      }
      if (semester != null) {
        predicates.add(cb.equal(module.get("semester"), semester));
      }
      if (groupName != null && !groupName.isBlank()) {
        predicates.add(cb.equal(student.get("groupName"), groupName));
      }
      if (elementId != null && !elementId.isBlank()) {
        predicates.add(cb.equal(element.get("id"), elementId));
      }
      if (query != null && !query.isBlank()) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        Predicate first = cb.like(cb.lower(student.get("firstName")), pattern);
        Predicate last = cb.like(cb.lower(student.get("lastName")), pattern);
        Predicate elem = cb.like(cb.lower(element.get("name")), pattern);
        Predicate mod = cb.like(cb.lower(module.get("name")), pattern);
        predicates.add(cb.or(first, last, elem, mod));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    return resultRepository.findAll(spec).stream()
      .sorted(
        Comparator.comparing((Result r) -> r.getStudent().getLastName())
          .thenComparing(r -> r.getElement().getModule().getSemester())
          .thenComparing(r -> r.getElement().getName())
      )
      .map(ResultResponse::new)
      .toList();
  }

  public List<ElementResponse> listControllableElements(User actor) {
    requireStaff(actor);

    List<Element> elements = actor.getRole() == Role.ADMIN
      ? elementRepository.findAll()
      : elementRepository.findByTeacher_Id(actor.getId());

    return elements.stream()
      .sorted(
        Comparator.comparing((Element e) -> e.getModule().getDepartment().name())
          .thenComparing(e -> e.getModule().getSemester())
          .thenComparing(Element::getName)
      )
      .map(ElementResponse::new)
      .toList();
  }

  public List<UserBaseResponse> listStudentsForGrading(
    User actor,
    DepartmentName department,
    Integer year,
    String groupName
  ) {
    requireStaff(actor);

    return userRepository.findByRole(Role.STUDENT).stream()
      .filter(s -> department == null || s.getDepartment() == department)
      .filter(s -> year == null || s.getYear() == year)
      .filter(s ->
        groupName == null
          || groupName.isBlank()
          || groupName.equalsIgnoreCase(s.getGroupName())
      )
      .sorted(
        Comparator.comparing(User::getLastName)
          .thenComparing(User::getFirstName)
      )
      .map(UserBaseResponse::new)
      .toList();
  }

  @Transactional
  public ResultResponse createResult(ResultCreateRequest req, User actor) {
    requireStaff(actor);
    validateGrade(req.grade());

    User student = userRepository.findById(req.studentId())
      .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    if (student.getRole() != Role.STUDENT) {
      throw new BadRequestException("Target user is not a student");
    }

    Element element = elementRepository.findById(req.elementId())
      .orElseThrow(() -> new ResourceNotFoundException("Element not found"));

    enforceElementOwnership(actor, element);

    if (student.getDepartment() != element.getModule().getDepartment()) {
      throw new BadRequestException(
        "Student department does not match element department"
      );
    }

    if (resultRepository.existsByStudent_IdAndElement_Id(
      student.getId(),
      element.getId()
    )) {
      throw new BadRequestException(
        "Grade already exists for this student and element"
      );
    }

    double rounded = roundTwoDecimals(req.grade());
    Result saved = resultRepository.save(
      new Result(rounded, student, element, actor, cleanText(req.remarks()))
    );

    return new ResultResponse(saved);
  }

  @Transactional
  public ResultResponse updateResult(
    String id,
    ResultUpdateRequest req,
    User actor
  ) {
    requireStaff(actor);
    validateGrade(req.grade());

    Result existing = resultRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Result not found"));

    enforceElementOwnership(actor, existing.getElement());

    existing.setGrade(roundTwoDecimals(req.grade()));
    if (req.remarks() != null) {
      existing.setRemarks(cleanText(req.remarks()));
    }
    existing.setLastModifiedBy(actor);
    Result saved = resultRepository.save(existing);

    return new ResultResponse(saved);
  }

  @Transactional
  public void deleteResult(String id, User actor) {
    requireStaff(actor);

    Result existing = resultRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Result not found"));

    enforceElementOwnership(actor, existing.getElement());
    resultRepository.delete(existing);
  }

  public ElementGradeStatsResponse getElementStats(
    String elementId,
    User actor
  ) {
    requireStaff(actor);
    Element element = elementRepository.findById(elementId)
      .orElseThrow(() -> new ResourceNotFoundException("Element not found"));
    enforceElementOwnership(actor, element);

    List<Result> results = resultRepository.findByElement_Id(elementId);
    if (results.isEmpty()) {
      return new ElementGradeStatsResponse(
        new ElementResponse(element),
        0,
        0.0,
        0.0,
        0.0,
        0.0
      );
    }

    double sum = 0.0;
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    int passed = 0;

    for (Result r : results) {
      double g = r.getGrade();
      sum += g;
      if (g < min) {
        min = g;
      }
      if (g > max) {
        max = g;
      }
      if (g >= 10.0) {
        passed++;
      }
    }

    double avg = roundTwoDecimals(sum / results.size());
    double passRate = roundTwoDecimals((passed * 100.0) / results.size());

    return new ElementGradeStatsResponse(
      new ElementResponse(element),
      results.size(),
      avg,
      roundTwoDecimals(min),
      roundTwoDecimals(max),
      passRate
    );
  }

  private SemesterSummaryResponse buildSemesterSummary(
    int semester,
    List<Result> results
  ) {
    List<ResultResponse> responses = results.stream()
      .sorted(
        Comparator.comparing(
          (Result r) -> r.getElement().getModule().getName()
        ).thenComparing(r -> r.getElement().getName())
      )
      .map(ResultResponse::new)
      .toList();

    Map<String, List<Result>> byModule = new LinkedHashMap<>();
    for (Result r : results) {
      String moduleId = r.getElement().getModule().getId();
      byModule.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(r);
    }

    List<ModuleAverageResponse> moduleAverages = new ArrayList<>();
    for (List<Result> group : byModule.values()) {
      Module module = group.get(0).getElement().getModule();
      double avg = roundTwoDecimals(
        group.stream().mapToDouble(Result::getGrade).average().orElse(0.0)
      );
      moduleAverages.add(
        new ModuleAverageResponse(
          new ModuleBaseResponse(module),
          avg,
          statusFromGrade(avg),
          group.size()
        )
      );
    }

    int passed = 0;
    int retake = 0;
    int failed = 0;
    double sum = 0.0;

    for (Result r : results) {
      double g = r.getGrade();
      sum += g;
      if (g >= 10.0) {
        passed++;
      } else if (g >= 7.0) {
        retake++;
      } else {
        failed++;
      }
    }

    double overallAverage = results.isEmpty()
      ? 0.0
      : roundTwoDecimals(sum / results.size());

    return new SemesterSummaryResponse(
      semester,
      overallAverage,
      mentionFromAverage(overallAverage),
      results.size(),
      passed,
      retake,
      failed,
      moduleAverages,
      responses
    );
  }

  private String statusFromGrade(double grade) {
    if (grade >= 10.0) {
      return "PASSED";
    }
    if (grade >= 7.0) {
      return "RETAKE";
    }
    return "FAILED";
  }

  private String mentionFromAverage(double average) {
    if (average >= 16.0) {
      return "TRES_BIEN";
    }
    if (average >= 14.0) {
      return "BIEN";
    }
    if (average >= 12.0) {
      return "ASSEZ_BIEN";
    }
    if (average >= 10.0) {
      return "PASSABLE";
    }
    return "AJOURNE";
  }

  private double roundTwoDecimals(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private void requireStaff(User actor) {
    if (actor.getRole() == Role.STUDENT) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Insufficient permissions"
      );
    }
  }

  private void enforceElementOwnership(User actor, Element element) {
    if (actor.getRole() == Role.ADMIN) {
      return;
    }
    if (!isTeacherOwner(actor, element)) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "You can only manage grades for your own elements"
      );
    }
  }

  private boolean isTeacherOwner(User teacher, Element element) {
    return element.getTeacher() != null
      && element.getTeacher().getId().equals(teacher.getId());
  }

  private void validateGrade(Double grade) {
    if (grade == null || Double.isNaN(grade) || grade < 0.0 || grade > 20.0) {
      throw new BadRequestException("Grade must be between 0 and 20");
    }
  }

  private String cleanText(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    return text.trim();
  }
}
