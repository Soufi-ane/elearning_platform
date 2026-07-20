package com.elearn.api.service;

import java.time.LocalDateTime;
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
    ){
    this.absenceRepository = absenceRepository;
    this.userRepository = userRepository;
    this.elementRepository = elementRepository;
  }

  public List<AbsenceByElementResponse> listByUser(String userId){
    List<Absence> absenceList = absenceRepository.findByStudentId(userId);

    Map<Element, Long> counts = absenceList.stream()
      .collect(Collectors.groupingBy(Absence::getElement, Collectors.counting()));

    return counts.entrySet().stream()
      .map(entry -> new AbsenceByElementResponse(
        new ElementResponse(entry.getKey()), 
        entry.getValue().intValue(), 
        getAbsenceStatus(entry.getValue().intValue())
      ))
      .collect(Collectors.toList());
  }

  private AbsenceStatus getAbsenceStatus(int count) {
    if(count > 6) return AbsenceStatus.DISCIPLINARY_HEARING;
    if(count > 4) return AbsenceStatus.WRITTEN_WARNING;
    if(count > 2) return AbsenceStatus.VERBAL_WARNING;
    return AbsenceStatus.NORMAL;
  }

  public List<AbsenceResponse> getByElementId(String elementId, String studentId){
    List<Absence> absences = absenceRepository.findByElementIdAndStudentId(elementId, studentId);
    return absences.stream()
      .map(a -> new AbsenceResponse(a))
      .toList();
  }

  @Transactional
  public AbsenceResponse createAbsence(AbsenceCreateRequest request) {
    User student = userRepository.findById(request.studentId())
      .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
    Element element = elementRepository.findById(request.elementId())
      .orElseThrow(() -> new ResourceNotFoundException("Element not found"));

    boolean exists = absenceRepository.existsByStudentIdAndElementIdAndDateTime(
      request.studentId(), 
      request.elementId(), 
      request.dateTime()
    );

    if (exists) {
      Absence existingAbsence = absenceRepository.findByStudentIdAndElementIdAndDateTime(
        request.studentId(), 
        request.elementId(), 
        request.dateTime()
      );
    return new AbsenceResponse(existingAbsence);
    }

    Absence newAbsence = new Absence();
    newAbsence.setStudent(student);
    newAbsence.setElement(element);
    
    newAbsence.setType(request.type() != null ? request.type() : AbsenceType.CLASS);
    
    newAbsence.setDateTime(request.dateTime() != null ? request.dateTime() : LocalDateTime.now());
    
    newAbsence.setJustified(false); 

    Absence savedAbsence = absenceRepository.save(newAbsence);
    return new AbsenceResponse(savedAbsence);
  }

  public List<AbsenceResponse> getAdminAbsences(String departmentName, String studentName) {
    List<Absence> absences;
    
    if (studentName != null && !studentName.isBlank()) {
      absences = absenceRepository.searchByStudentName(studentName);
    } else if (departmentName != null && !departmentName.isBlank()) {
      DepartmentName deptEnum = DepartmentName.valueOf(departmentName.toUpperCase());
      absences = absenceRepository.findByStudentDepartment(deptEnum);
    } else {
      absences = absenceRepository.findAll();
    }

    return absences.stream()
      .map(AbsenceResponse::new)
      .toList();
  }

  public List<AbsenceResponse> getTeacherAbsences(String teacherId) {
    return absenceRepository.findByTeacherId(teacherId).stream()
      .map(AbsenceResponse::new)
      .toList();
  }

  @Transactional
  public AbsenceResponse updateAbsence(String id, AbsenceUpdateRequest request) {
    Absence absence = absenceRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Absence not found"));

    if (request.isJustified() != null) {
      absence.setJustified(request.isJustified());
    }
    if (request.type() != null) {
      absence.setType(request.type());
    }

    Absence savedAbsence = absenceRepository.save(absence);
    return new AbsenceResponse(savedAbsence);
  }

  public void deleteAbsence(String absenceId) {
    if (!absenceRepository.existsById(absenceId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence record not found.");
    }
    absenceRepository.deleteById(absenceId);
  }
}
