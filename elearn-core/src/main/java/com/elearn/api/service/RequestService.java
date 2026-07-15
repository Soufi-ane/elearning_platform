package com.elearn.api.service;

import com.elearn.api.entity.Schemas.UserRequest;
import com.elearn.api.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

  private final RequestRepository requestRepository;

  public List<UserRequest> getRequestsByStudent(String studentId) {
    return requestRepository.findByStudentId(studentId)
      .stream()
      .map(UserRequest::new)
      .toList();
  }

}
