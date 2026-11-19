package com.elearn.api.controller;

import java.util.List;
import com.elearn.api.entity.Schemas.ProfileOptionsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.Schemas.LoginRequest;
import com.elearn.api.entity.Schemas.PasswordChangeRequest;
import com.elearn.api.entity.Schemas.RegisterRequest;
import com.elearn.api.entity.Schemas.UserResponse;
import com.elearn.api.entity.Schemas.UserUpdateRequest;
import com.elearn.api.entity.User;
import com.elearn.api.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/options")
  public ProfileOptionsResponse getProfileOptions() {
    return userService.getProfileOptions();
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(
    @RequestBody RegisterRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return userService.register(request, (User) userDetails);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(
    @PathVariable("id") String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    userService.deleteUser(id, (User) userDetails);
  }

  @GetMapping("/auth")
  public ResponseEntity<?> authenticate(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(new UserResponse((User) userDetails));
  }

  @PostMapping("/login")
  public UserResponse login(
    @RequestBody LoginRequest request,
    HttpServletResponse response
  ) {
    return userService.login(request, response);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    return userService.logout(response);
  }

  @GetMapping
  public ResponseEntity<?> getUsers(
    @RequestParam(required = false) Role role,
    @RequestParam(required = false) String query,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    if (userDetails == null) {
      return ResponseEntity.status(401).build();
    }
    User currentUser = (User) userDetails;
    if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.TEACHER) {
      return ResponseEntity.status(403).body("Access denied");
    }
    List<UserResponse> users = userService.getUsers(role, query);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  public UserResponse getProfile(
    @PathVariable("id") String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return userService.getUserProfile(id, (User) userDetails);
  }

  @PutMapping("/{id}")
  public UserResponse updateProfile(
    @PathVariable("id") String id,
    @RequestBody UserUpdateRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return userService.updateUserProfile(id, request, (User) userDetails);
  }

  @PutMapping("/me/password")
  public ResponseEntity<?> changePassword(
    @RequestBody PasswordChangeRequest request,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    userService.changeOwnPassword(request, (User) userDetails);
    return ResponseEntity.ok().body("Password updated");
  }

  @GetMapping("/{id}/picture")
  public ResponseEntity<byte[]> getPicture(
    @PathVariable("id") String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return userService.getProfilePicture(id, (User) userDetails);
  }

  @PutMapping("/{id}/picture")
  public UserResponse uploadPicture(
    @PathVariable("id") String id,
    @RequestParam("file") MultipartFile file,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return userService.updateProfilePicture(id, file, (User) userDetails);
  }

  @DeleteMapping("/{id}/picture")
  public UserResponse deletePicture(
    @PathVariable("id") String id,
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    return userService.deleteProfilePicture(id, (User) userDetails);
  }
}
