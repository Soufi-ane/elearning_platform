package com.elearn.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.LoginRequest;
import com.elearn.api.entity.Schemas.RegisterRequest;
import com.elearn.api.entity.Schemas.UserBaseResponse;
import com.elearn.api.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  @Autowired
  public UserController(UserService userService){
    this.userService = userService;
  }

  @PostMapping("/register")
  public UserBaseResponse register(@RequestBody RegisterRequest request){
    return userService.register(request);
  }

  @GetMapping("/auth")
  public ResponseEntity<?> authenticate(@AuthenticationPrincipal UserDetails userDetails){
    if(userDetails == null) return ResponseEntity.status(401).build();
    return ResponseEntity.ok(new UserBaseResponse((User) userDetails));
  }

  @PostMapping("/login")
  public UserBaseResponse login(@RequestBody LoginRequest request, HttpServletResponse response){
    return userService.login(request,response);
  }

}
