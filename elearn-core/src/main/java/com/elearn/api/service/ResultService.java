package com.elearn.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Schemas.ResultResponse;
import com.elearn.api.repository.ResultRepository;

@Service 
public class ResultService {
  private final ResultRepository resultRepository;

  @Autowired
  public ResultService(ResultRepository resultRepository){
    this.resultRepository = resultRepository;
  }

  public List<ResultResponse> listBySemester(int semester, String userId){
    return resultRepository.findByElement_Module_SemesterAndStudent_Id(semester, userId)
      .stream()
      .map(r -> new ResultResponse(r))
      .toList();
  }

}
