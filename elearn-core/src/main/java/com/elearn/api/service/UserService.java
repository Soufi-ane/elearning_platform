package com.elearn.api.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Department;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.LoginRequest;
import com.elearn.api.entity.Schemas.RegisterRequest;
import com.elearn.api.entity.Schemas.UserResponse;
import com.elearn.api.exception.BadRequestException;
import com.elearn.api.exception.DepartmentNotFoundException;
import com.elearn.api.exception.InvalidCredentialsException;
import com.elearn.api.exception.UsernameOrEmailTakenException;
import com.elearn.api.repository.DepartmentRepository;
import com.elearn.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

@Service 
public class UserService {
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  @Value("${ENVIRONMENT}")
  private String environment;

  @Autowired
  public UserService(
    UserRepository userRepository,
    DepartmentRepository departmentRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService
  ){
    this.userRepository = userRepository;
    this.departmentRepository = departmentRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public UserResponse register(RegisterRequest request){
    Optional<User> optUser = userRepository
      .findByUsernameOrEmail(request.username(),request.email());
    if(optUser.isPresent()) throw new UsernameOrEmailTakenException(
      optUser.get().getUsername().equals(request.username()) ?
      "Username taken" : "Email taken"
    );
    User user = new User(
      request.firstName(),request.lastName(),request.username(),
      request.email(), passwordEncoder.encode(request.password()),
      request.dateOfBirth(),request.role(),request.studyMode(),request.year()
    );
    if(request.departmentId() != null) {
      Optional<Department> optDepartment = departmentRepository.findById(request.departmentId());
      if(optDepartment.isPresent()) user.setDepartment(optDepartment.get());
      else throw new DepartmentNotFoundException("Department not found");
    }else {
      if(request.role() == Role.STUDENT || request.role() == Role.TEACHER) {
        throw new BadRequestException("A department is required for this role");
      }
    }
    return new UserResponse(userRepository.save(user));
  }

  public UserResponse login(LoginRequest request,HttpServletResponse response){
    Optional<User> optUser = userRepository
      .findByUsernameOrEmail(request.usernameOrEmail(),request.usernameOrEmail());
    if(!optUser.isPresent()) throw new InvalidCredentialsException("Invalid username or password");
    User user = optUser.get();
    if(!passwordEncoder.matches(request.password(), user.getPassword())){
      throw new InvalidCredentialsException("Invalid username or password");
    }

    String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
    ResponseCookie cookie = ResponseCookie.from("jwt",token)
      .httpOnly(true)
      .secure(!environment.equals("DEV"))
      .path("/")
      .maxAge(Duration.ofDays(1))
      .sameSite("Strict")
      .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    
    return new UserResponse(user);
  }

  public ResponseEntity<?> logout(HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("jwt","")
      .httpOnly(true)
      .secure(!environment.equals("DEV"))
      .path("/")
      .maxAge(0)
      .sameSite("Strict")
      .build();
    
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ResponseEntity.ok().body("Logged out successfully");
  }

}
