package com.elearn.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Absence;
import com.elearn.api.entity.Schemas.AbsenceResponse;
import com.elearn.api.repository.AbsenceRepository;

@Service 
public class AbsenceService {
  private final AbsenceRepository absenceRepository;

  @Autowired
  public AbsenceService(AbsenceRepository absenceRepository){
    this.absenceRepository = absenceRepository;
  }

  public List<AbsenceResponse> listByUser(String userId){
    List<Absence> absenceList = absenceRepository.findByStudentId(userId);
    return absenceList.stream()
      .map(a -> new AbsenceResponse(a))
      .toList();
  }

}
