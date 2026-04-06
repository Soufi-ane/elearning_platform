package com.elearn.api.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.Department;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.LoginRequest;
import com.elearn.api.entity.Schemas.LoginResponse;
import com.elearn.api.entity.Schemas.RegisterRequest;
import com.elearn.api.entity.Schemas.UserBaseResponse;
import com.elearn.api.exception.BadRequestException;
import com.elearn.api.exception.DepartmentNotFoundException;
import com.elearn.api.exception.InvalidCredentialsException;
import com.elearn.api.exception.UsernameOrEmailTakenException;
import com.elearn.api.repository.DepartmentRepository;
import com.elearn.api.repository.UserRepository;

@Service 
public class UserService {
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

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

  public UserBaseResponse register(RegisterRequest request){
    Optional<User> optUser = userRepository
      .findByUsernameOrEmail(request.username(),request.email());
    if(optUser.isPresent()) throw new UsernameOrEmailTakenException(
      optUser.get().getUsername().equals(request.username()) ?
      "Username taken" : "Email taken"
    );
    User user = new User(
      request.firstName(),request.lastName(),request.username(),
      request.email(), passwordEncoder.encode(request.password()),
      request.dateOfBirth(),request.role()
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
    return new UserBaseResponse(userRepository.save(user));
  }

  public LoginResponse login(LoginRequest request){
    Optional<User> optUser = userRepository
      .findByUsernameOrEmail(request.usernameOrEmail(),request.usernameOrEmail());
    if(!optUser.isPresent()) throw new InvalidCredentialsException("Invalid username or password");
    User user = optUser.get();
    if(!passwordEncoder.matches(request.password(), user.getPassword())){
      throw new InvalidCredentialsException("Invalid username or password");
    }
    String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
    return new LoginResponse(new UserBaseResponse(user), token);
  }

}
