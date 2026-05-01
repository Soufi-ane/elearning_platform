package com.elearn.api.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Absence;
import com.elearn.api.entity.AbsenceStatus;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Schemas.AbsenceByElementResponse;
import com.elearn.api.entity.Schemas.AbsenceResponse;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.repository.AbsenceRepository;

@Service 
public class AbsenceService {
  private final AbsenceRepository absenceRepository;

  @Autowired
  public AbsenceService(AbsenceRepository absenceRepository){
    this.absenceRepository = absenceRepository;
  }

  public List<AbsenceByElementResponse> listByUser(String userId){
    List<Absence> absenceList = absenceRepository.findByStudentId(userId);
    List<AbsenceByElementResponse> finalList = new ArrayList<AbsenceByElementResponse>();
    int count = 0;
    Element currentElement = null;
    if(!absenceList.isEmpty()) currentElement= absenceList.get(0).getElement();
    for(Absence absence : absenceList){
      if(!absence.getElement().getId().equals(currentElement.getId())){
        finalList.add(
          new AbsenceByElementResponse(
            new ElementResponse(currentElement), count, getAbsenceStatus(count)
          )
        );
        count = 1;
        currentElement = absence.getElement();
      }else {
        count++;
      }
    }
    if(!absenceList.isEmpty()) {
      finalList.add(
        new AbsenceByElementResponse(
          new ElementResponse(currentElement), count, getAbsenceStatus(count)
        )
      );
    }
    return finalList;
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

}
