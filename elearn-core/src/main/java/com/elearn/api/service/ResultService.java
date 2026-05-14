package com.elearn.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.repository.ResultRepository;

@Service 
public class ResultService {
  private final ResultRepository resultRepository;

  @Autowired
  public ResultService(ResultRepository resultRepository){
    this.resultRepository = resultRepository;
  }

}
