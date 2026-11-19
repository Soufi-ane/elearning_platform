package com.elearn.api.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.Schemas.LoginRequest;
import com.elearn.api.entity.Schemas.PasswordChangeRequest;
import com.elearn.api.entity.Schemas.ProfileOptionsResponse;
import com.elearn.api.entity.Schemas.RegisterRequest;
import com.elearn.api.entity.Schemas.UserResponse;
import com.elearn.api.entity.Schemas.UserUpdateRequest;
import com.elearn.api.entity.StudyMode;
import com.elearn.api.entity.User;
import com.elearn.api.exception.BadRequestException;
import com.elearn.api.exception.InvalidCredentialsException;
import com.elearn.api.exception.ResourceNotFoundException;
import com.elearn.api.exception.UsernameOrEmailTakenException;
import com.elearn.api.repository.ResultRepository;
import com.elearn.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserService {
  private static final long MAX_PICTURE_BYTES = 2L * 1024L * 1024L;

  private final UserRepository userRepository;
  private final ResultRepository resultRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  @Value("${ENVIRONMENT}")
  private String environment;

  @Autowired
  public UserService(
    UserRepository userRepository,
    ResultRepository resultRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.resultRepository = resultRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public UserResponse register(RegisterRequest request, User actor) {
    if (actor == null || actor.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Only admins can create accounts"
      );
    }
    if (
      request.firstName() == null || request.firstName().isBlank()
        || request.lastName() == null || request.lastName().isBlank()
        || request.username() == null || request.username().isBlank()
        || request.email() == null || request.email().isBlank()
        || request.password() == null || request.password().length() < 6
        || request.role() == null
    ) {
      throw new BadRequestException(
        "Required fields are missing or password is under 6 characters"
      );
    }

    String username = request.username().trim();
    String email = request.email().trim();
    Optional<User> optUser = userRepository
      .findByUsernameOrEmail(username, email);
    if (optUser.isPresent()) {
      throw new UsernameOrEmailTakenException(
        optUser.get().getDbUsername().equalsIgnoreCase(username)
          ? "Username taken"
          : "Email taken"
      );
    }

    Role role = request.role();
    if ((role == Role.STUDENT || role == Role.TEACHER)
      && request.department() == null) {
      throw new BadRequestException("A department is required for this role");
    }

    User user = new User(request, passwordEncoder.encode(request.password()));
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setUsername(username);
    user.setEmail(email);
    user.setRootAdmin(false);

    if (role == Role.ADMIN) {
      user.setDepartment(null);
      user.setStudyMode(null);
      user.setYear(0);
      user.setSemester(0);
      user.setNumSemesters(0);
      user.setGroupName(null);
    } else if (role == Role.TEACHER) {
      user.setDepartment(request.department());
      user.setStudyMode(null);
      user.setYear(0);
      user.setSemester(0);
      user.setNumSemesters(0);
      user.setGroupName(null);
    } else {
      user.setDepartment(request.department());
      user.setNumSemesters(request.department().getTotalSemesters());
      user.setYear(request.year() > 0 ? request.year() : 1);
      user.setSemester(request.semester() > 0 ? request.semester() : 1);
      user.setGroupName(
        request.groupName() != null && !request.groupName().isBlank()
          ? request.groupName().trim()
          : "A"
      );
    }

    return new UserResponse(userRepository.save(user));
  }

  public UserResponse login(LoginRequest request, HttpServletResponse response) {
    Optional<User> optUser = userRepository.findByUsernameOrEmail(
      request.usernameOrEmail(),
      request.usernameOrEmail()
    );
    if (optUser.isEmpty()) {
      throw new InvalidCredentialsException("Invalid username or password");
    }
    User user = optUser.get();
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new InvalidCredentialsException("Invalid username or password");
    }

    String token = jwtService.generateToken(
      user.getEmail(),
      user.getId(),
      user.getRole()
    );
    ResponseCookie cookie = ResponseCookie.from("jwt", token)
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
    ResponseCookie cookie = ResponseCookie.from("jwt", "")
      .httpOnly(true)
      .secure(!environment.equals("DEV"))
      .path("/")
      .maxAge(0)
      .sameSite("Strict")
      .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ResponseEntity.ok().body("Logged out successfully");
  }

  public List<UserResponse> getUsers(Role role, String query) {
    List<User> users = role != null
      ? userRepository.findByRole(role)
      : userRepository.findAll();

    String filter = query == null ? "" : query.trim().toLowerCase();

    return users.stream()
      .filter(u -> filter.isEmpty() || matchesQuery(u, filter))
      .sorted(
        Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
          .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER)
      )
      .map(UserResponse::new)
      .toList();
  }

  public UserResponse getUserProfile(String targetId, User actor) {
    requireSelfOrAdmin(targetId, actor);
    User user = userRepository.findById(targetId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return new UserResponse(user);
  }

  public UserResponse updateUserProfile(
    String targetId,
    UserUpdateRequest req,
    User actor
  ) {
    if (actor.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Only admins can edit profile details"
      );
    }

    User target = userRepository.findById(targetId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (req.username() != null && !req.username().isBlank()) {
      String nextUsername = req.username().trim();
      if (!nextUsername.equalsIgnoreCase(target.getDbUsername())) {
        Optional<User> clash = userRepository
          .findByUsernameOrEmail(nextUsername, nextUsername);
        if (clash.isPresent() && !clash.get().getId().equals(target.getId())) {
          throw new UsernameOrEmailTakenException("Username taken");
        }
        target.setUsername(nextUsername);
      }
    }

    if (req.email() != null && !req.email().isBlank()) {
      String nextEmail = req.email().trim();
      if (!nextEmail.equalsIgnoreCase(target.getEmail())) {
        Optional<User> clash = userRepository
          .findByUsernameOrEmail(nextEmail, nextEmail);
        if (clash.isPresent() && !clash.get().getId().equals(target.getId())) {
          throw new UsernameOrEmailTakenException("Email taken");
        }
        target.setEmail(nextEmail);
      }
    }

    if (req.firstName() != null && !req.firstName().isBlank()) {
      target.setFirstName(req.firstName().trim());
    }
    if (req.lastName() != null && !req.lastName().isBlank()) {
      target.setLastName(req.lastName().trim());
    }
    if (req.dateOfBirth() != null) {
      target.setDateOfBirth(req.dateOfBirth());
    }
    if (target.isRootAdmin() && req.role() != null && req.role() != Role.ADMIN) {
      throw new BadRequestException("Root admin role cannot be changed");
    }
    if (req.role() != null) {
      target.setRole(req.role());
    }

    Role effectiveRole = target.getRole();
    if (effectiveRole == Role.ADMIN) {
      target.setDepartment(null);
      target.setStudyMode(null);
      target.setYear(0);
      target.setSemester(0);
      target.setNumSemesters(0);
      target.setGroupName(null);
    } else {
      if (req.department() != null) {
        target.setDepartment(req.department());
        target.setNumSemesters(req.department().getTotalSemesters());
      } else if (target.getDepartment() == null) {
        throw new BadRequestException("A department is required for this role");
      }

      if (effectiveRole == Role.STUDENT) {
        if (req.studyMode() != null) {
          target.setStudyMode(req.studyMode());
        }
        if (req.year() != null && req.year() > 0) {
          target.setYear(req.year());
        }
        if (req.semester() != null && req.semester() > 0) {
          target.setSemester(req.semester());
        }
        if (req.groupName() != null && !req.groupName().isBlank()) {
          target.setGroupName(req.groupName().trim());
        }
      } else {
        target.setStudyMode(null);
        target.setYear(0);
        target.setSemester(0);
        target.setGroupName(null);
      }
    }

    return new UserResponse(userRepository.save(target));
  }

  public void changeOwnPassword(PasswordChangeRequest req, User actor) {
    if (req.currentPassword() == null || req.newPassword() == null) {
      throw new BadRequestException("Current and new password are required");
    }
    if (req.newPassword().length() < 6) {
      throw new BadRequestException(
        "New password must be at least 6 characters"
      );
    }

    User user = userRepository.findById(actor.getId())
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(req.newPassword()));
    userRepository.save(user);
  }

  public UserResponse updateProfilePicture(
    String targetId,
    MultipartFile file,
    User actor
  ) {
    requireSelfOrAdmin(targetId, actor);

    if (file == null || file.isEmpty()) {
      throw new BadRequestException("Image file is required");
    }
    if (file.getSize() > MAX_PICTURE_BYTES) {
      throw new BadRequestException("Image must be 2 MB or smaller");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new BadRequestException("Only image files are allowed");
    }

    User target = userRepository.findById(targetId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    try {
      target.setProfilePicture(file.getBytes());
      target.setProfilePictureType(contentType);
    } catch (IOException e) {
      throw new BadRequestException("Failed to read uploaded image");
    }

    return new UserResponse(userRepository.save(target));
  }

  public UserResponse deleteProfilePicture(String targetId, User actor) {
    requireSelfOrAdmin(targetId, actor);
    User target = userRepository.findById(targetId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    target.setProfilePicture(null);
    target.setProfilePictureType(null);
    return new UserResponse(userRepository.save(target));
  }

  public ResponseEntity<byte[]> getProfilePicture(String targetId, User actor) {
    requireSelfOrAdmin(targetId, actor);
    User target = userRepository.findById(targetId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    byte[] bytes = target.getProfilePicture();
    if (bytes == null || bytes.length == 0) {
      return ResponseEntity.notFound().build();
    }

    MediaType mediaType = target.getProfilePictureType() != null
      ? MediaType.parseMediaType(target.getProfilePictureType())
      : MediaType.APPLICATION_OCTET_STREAM;

    return ResponseEntity.ok()
      .contentType(mediaType)
      .cacheControl(CacheControl.noCache())
      .body(bytes);
  }

  private void requireSelfOrAdmin(String targetId, User actor) {
    if (actor.getRole() == Role.ADMIN || actor.getId().equals(targetId)) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  private boolean matchesQuery(User u, String q) {
    return contains(u.getFirstName(), q)
      || contains(u.getLastName(), q)
      || contains(u.getDbUsername(), q)
      || contains(u.getEmail(), q);
  }

  private boolean contains(String field, String q) {
    return field != null && field.toLowerCase().contains(q);
  }

  public ProfileOptionsResponse getProfileOptions() {
    List<String> roles = Arrays.stream(Role.values())
      .map(Enum::name)
      .toList();
    List<String> departments = Arrays.stream(DepartmentName.values())
      .map(Enum::name)
      .toList();
    List<String> studyModes = Arrays.stream(StudyMode.values())
      .map(Enum::name)
      .toList();
    return new ProfileOptionsResponse(roles, departments, studyModes);
  }

  @Transactional
  public void deleteUser(String targetId, User actor) {
    if (actor == null || actor.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Only admins can delete accounts"
      );
    }

    User target = userRepository.findById(targetId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (target.isRootAdmin()) {
      throw new BadRequestException("Root admin account cannot be deleted");
    }
    if (target.getId().equals(actor.getId())) {
      throw new BadRequestException("You cannot delete your own account");
    }

    resultRepository.clearGradedBy(target.getId());
    resultRepository.clearLastModifiedBy(target.getId());
    userRepository.delete(target);
  }
}
