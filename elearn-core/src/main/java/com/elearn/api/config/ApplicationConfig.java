package com.elearn.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.elearn.api.repository.UserRepository;

@Configuration
public class ApplicationConfig {

  private final UserRepository userRepository;

  public ApplicationConfig(UserRepository userRepository){
    this. userRepository = userRepository;
  }

  @Bean 
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(){
    return identifier -> userRepository.findByEmail(identifier)
      .orElseThrow(()-> new UsernameNotFoundException("User not found"));
  }

}
