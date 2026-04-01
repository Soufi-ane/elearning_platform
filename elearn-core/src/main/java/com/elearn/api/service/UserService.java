package com.elearn.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.repository.UserRepository;

@Service 
public class UserService {
  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository){
    this.userRepository = userRepository;
  }
}
