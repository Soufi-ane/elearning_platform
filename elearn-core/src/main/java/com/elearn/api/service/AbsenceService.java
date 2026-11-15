package com.elearn.api.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.Absence;
import com.elearn.api.entity.AbsenceStatus;
import com.elearn.api.entity.AbsenceType;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.AbsenceByElementResponse;
import com.elearn.api.entity.Schemas.AbsenceCreateRequest;
import com.elearn.api.entity.Schemas.AbsenceResponse;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.AbsenceCreateRequest.AbsenceUpdateRequest;
import com.elearn.api.exception.ResourceNotFoundException;
import com.elearn.api.repository.AbsenceRepository;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class AbsenceService {

  private final AbsenceRepository absenceRepository;
  private final UserRepository userRepository;
  private final ElementRepository elementRepository;

  @Autowired
  public AbsenceService(
    AbsenceRepository absenceRepository,
    UserRepository userRepository,
    ElementRepository elementRepository
  ) {
    this.absenceRepository = absenceRepository;
    this.userRepository = userRepository;
    this.elementRepository = elementRepository;
  }

  public List<AbsenceByElementResponse> listByUser(String userId) {
    if (userId == null || userId.isBlank()) {
      return List.of();
    }
    List<Absence> absenceList = absenceRepository.findByStudentId(userId);

    Map<Element, Long> counts = absenceList.stream()
      .collect(
        Collectors.groupingBy(Absence::getElement, Collectors.counting())
      );

    return counts.entrySet().stream()
      .map(entry -> new AbsenceByElementResponse(
        new ElementResponse(entry.getKey()),
        entry.getValue().intValue(),
        getAbsenceStatus(entry.getValue().intValue())
      ))
      .collect(Collectors.toList());
  }

  private AbsenceStatus getAbsenceStatus(int count) {
    if (count > 6) return AbsenceStatus.DISCIPLINARY_HEARING;
    if (count > 4) return AbsenceStatus.WRITTEN_WARNING;
    if (count > 2) return AbsenceStatus.VERBAL_WARNING;
    return AbsenceStatus.NORMAL;
  }

  public List<AbsenceResponse> getByElementId(
    String elementId,
    String studentId
  ) {
    List<Absence> absences = (studentId != null && !studentId.isBlank())
      ? absenceRepository.findByElementIdAndStudentId(elementId, studentId)
      : absenceRepository.findAll().stream()
          .filter(
            a -> a.getElement() != null &&
                 a.getElement().getId().equals(elementId)
          )
          .toList();

    return absences.stream()
      .sorted(Comparator.comparing(Absence::getDateTime).reversed())
      .map(AbsenceResponse::new)
      .toList();
  }

  public List<AbsenceResponse> getManagedAbsences(
    User actor,
    DepartmentName department,
    Integer year,
    Integer semester,
    String groupName,
    String elementId,
    String studentId,
    String searchName
  ) {
    List<Absence> base = (actor.getRole() == Role.TEACHER)
      ? absenceRepository.findByTeacherId(actor.getId())
      : absenceRepository.findAll();

    String q = (searchName != null) ? searchName.trim().toLowerCase() : "";

    return base.stream()
      .filter(a -> {
        if (department == null) return true;
        DepartmentName stDept = (a.getStudent() != null)
          ? a.getStudent().getDepartment()
          : null;
        return department == stDept;
      })
      .filter(a -> {
        if (year == null || year <= 0) return true;
        int stYear = (a.getStudent() != null) ? a.getStudent().getYear() : 0;
        return stYear == year;
      })
      .filter(a -> {
        if (semester == null || semester <= 0) return true;
        int elSem = (
          a.getElement() != null && a.getElement().getModule() != null
        ) ? a.getElement().getModule().getSemester() : 0;
        int stSem = (a.getStudent() != null) ? a.getStudent().getSemester() : 0;
        return elSem > 0 ? elSem == semester : stSem == semester;
      })
      .filter(a -> {
        if (
          groupName == null ||
          groupName.isBlank() ||
          "ALL".equalsIgnoreCase(groupName)
        ) {
          return true;
        }
        String stGroup = (
          a.getStudent() != null && a.getStudent().getGroupName() != null
        ) ? a.getStudent().getGroupName().trim() : "A";
        return groupName.trim().equalsIgnoreCase(stGroup);
      })
      .filter(a -> {
        if (elementId == null || elementId.isBlank()) return true;
        return a.getElement() != null &&
          elementId.equals(a.getElement().getId());
      })
      .filter(a -> {
        if (studentId == null || studentId.isBlank()) return true;
        return a.getStudent() != null &&
          studentId.equals(a.getStudent().getId());
      })
      .filter(a -> {
        if (q.isEmpty()) return true;
        if (a.getStudent() == null) return false;
        String full = (
          a.getStudent().getFirstName() + " " + a.getStudent().getLastName()
        ).toLowerCase();
        String rev = (
          a.getStudent().getLastName() + " " + a.getStudent().getFirstName()
        ).toLowerCase();
        String uname = a.getStudent().getDbUsername() != null
          ? a.getStudent().getDbUsername().toLowerCase()
          : "";
        return full.contains(q) || rev.contains(q) || uname.contains(q);
      })
      .sorted(Comparator.comparing(Absence::getDateTime).reversed())
      .map(AbsenceResponse::new)
      .toList();
  }

  @Transactional
  public AbsenceResponse createAbsence(
    AbsenceCreateRequest request,
    User actor
  ) {
    User student = userRepository.findById(request.studentId())
      .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    Element element = elementRepository.findById(request.elementId())
      .orElseThrow(() -> new ResourceNotFoundException("Element not found"));

    validateTeacherOwnership(actor, element);

    LocalDateTime dt = request.dateTime() != null
      ? request.dateTime()
      : LocalDateTime.now().withSecond(0).withNano(0);

    boolean exists = absenceRepository.existsByStudentIdAndElementIdAndDateTime(
      request.studentId(),
      request.elementId(),
      dt
    );
    if (exists) {
      Absence existing =
        absenceRepository.findByStudentIdAndElementIdAndDateTime(
          request.studentId(),
          request.elementId(),
          dt
        );
      return new AbsenceResponse(existing);
    }

    Absence newAbsence = new Absence();
    newAbsence.setStudent(student);
    newAbsence.setElement(element);
    newAbsence.setType(
      request.type() != null ? request.type() : AbsenceType.CLASS
    );
    newAbsence.setDateTime(dt);
    newAbsence.setJustified(Boolean.TRUE.equals(request.isJustified()));

    return new AbsenceResponse(absenceRepository.save(newAbsence));
  }

  @Transactional
  public AbsenceResponse updateAbsence(
    String id,
    AbsenceUpdateRequest request,
    User actor
  ) {
    Absence absence = absenceRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Absence not found"));

    validateTeacherOwnership(actor, absence.getElement());

    if (request.studentId() != null && !request.studentId().isBlank()) {
      User student = userRepository.findById(request.studentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
      absence.setStudent(student);
    }

    if (request.elementId() != null && !request.elementId().isBlank()) {
      Element element = elementRepository.findById(request.elementId())
        .orElseThrow(() -> new ResourceNotFoundException("Element not found"));
      validateTeacherOwnership(actor, element);
      absence.setElement(element);
    }

    if (request.dateTime() != null) {
      absence.setDateTime(request.dateTime());
    }
    if (request.isJustified() != null) {
      absence.setJustified(request.isJustified());
    }
    if (request.type() != null) {
      absence.setType(request.type());
    }

    return new AbsenceResponse(absenceRepository.save(absence));
  }

  public void deleteAbsence(String absenceId, User actor) {
    Absence absence = absenceRepository.findById(absenceId)
      .orElseThrow(
        () -> new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Absence record not found."
        )
      );
    validateTeacherOwnership(actor, absence.getElement());
    absenceRepository.delete(absence);
  }

  private void validateTeacherOwnership(User actor, Element element) {
    if (actor.getRole() == Role.TEACHER) {
      if (
        element == null ||
        element.getTeacher() == null ||
        !element.getTeacher().getId().equals(actor.getId())
      ) {
        throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Teachers can only manage absences for their own course elements."
        );
      }
    }
  }
}
