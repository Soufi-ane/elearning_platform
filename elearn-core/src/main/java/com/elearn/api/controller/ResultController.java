package com.elearn.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.service.ResultService;

@RestController
@RequestMapping("/api/v1/reuslts")
public class ResultController {
  private final ResultService resultService;

  @Autowired
  public ResultController(ResultService resultService){
    this.resultService = resultService;
  }

}
