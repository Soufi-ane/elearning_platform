package com.elearn.api.service;

import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.Request;
import com.elearn.api.entity.RequestState;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.RequestCreateDto;
import com.elearn.api.entity.Schemas.RequestUpdateDto;
import com.elearn.api.entity.Schemas.UserRequest;
import com.elearn.api.exception.BadRequestException;
import com.elearn.api.exception.ResourceNotFoundException;
import com.elearn.api.repository.RequestRepository;
import com.elearn.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestService {
  private final RequestRepository requestRepository;
  private final UserRepository userRepository;

  public List<UserRequest> getRequests(
    User actor,
    String userId,
    RequestState state,
    Role role,
    String search
  ) {
    List<Request> base;
    if (actor.getRole() == Role.ADMIN) {
      if (userId != null && !userId.isBlank()) {
        base = requestRepository.findByStudentIdOrderByRequestDateDesc(userId);
      } else {
        base = requestRepository.findAllByOrderByRequestDateDesc();
      }
    } else {
      base = requestRepository.findByStudentIdOrderByRequestDateDesc(
        actor.getId()
      );
    }

    String q = (search != null) ? search.trim().toLowerCase() : "";

    return base.stream()
      .filter(r -> state == null || r.getState() == state)
      .filter(r -> {
        if (role == null || actor.getRole() != Role.ADMIN) return true;
        return r.getStudent() != null && r.getStudent().getRole() == role;
      })
      .filter(r -> {
        if (q.isEmpty()) return true;
        boolean matchTitle = r.getTitle() != null &&
          r.getTitle().toLowerCase().contains(q);
        boolean matchUser = false;
        if (r.getStudent() != null) {
          String full = (
            r.getStudent().getFirstName() + " " + r.getStudent().getLastName()
          ).toLowerCase();
          matchUser = full.contains(q);
        }
        return matchTitle || matchUser;
      })
      .map(UserRequest::new)
      .toList();
  }

  public UserRequest createRequest(RequestCreateDto dto, User actor) {
    if (dto.title() == null || dto.title().isBlank()) {
      throw new BadRequestException("Request title is required");
    }

    User owner = actor;
    if (
      actor.getRole() == Role.ADMIN &&
      dto.userId() != null &&
      !dto.userId().isBlank()
    ) {
      owner = userRepository.findById(dto.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    RequestState initialState = (
      actor.getRole() == Role.ADMIN && dto.state() != null
    ) ? dto.state() : RequestState.PENDING;

    Date forecast = (actor.getRole() == Role.ADMIN) ? dto.forcastDate() : null;

    Request req = new Request(
      null,
      dto.title().trim(),
      initialState,
      new Date(),
      forecast,
      owner
    );
    return new UserRequest(requestRepository.save(req));
  }

  public UserRequest updateRequest(
    String id,
    RequestUpdateDto dto,
    User actor
  ) {
    Request req = requestRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

    if (actor.getRole() != Role.ADMIN) {
      if (
        req.getStudent() == null ||
        !req.getStudent().getId().equals(actor.getId())
      ) {
        throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "You can only modify your own requests"
        );
      }
      if (req.getState() != RequestState.PENDING) {
        throw new BadRequestException(
          "Cannot modify a request once administration has started processing it"
        );
      }
      if (dto.title() == null || dto.title().isBlank()) {
        throw new BadRequestException("Request title is required");
      }
      req.setTitle(dto.title().trim());
      return new UserRequest(requestRepository.save(req));
    }

    if (dto.title() != null && !dto.title().isBlank()) {
      req.setTitle(dto.title().trim());
    }
    if (dto.state() != null) {
      req.setState(dto.state());
    }
    req.setForcastDate(dto.forcastDate());

    if (dto.userId() != null && !dto.userId().isBlank()) {
      User newOwner = userRepository.findById(dto.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      req.setStudent(newOwner);
    }

    return new UserRequest(requestRepository.save(req));
  }

  public void deleteRequest(String id, User actor) {
    Request req = requestRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

    if (actor.getRole() != Role.ADMIN) {
      if (
        req.getStudent() == null ||
        !req.getStudent().getId().equals(actor.getId())
      ) {
        throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "You can only delete your own requests"
        );
      }
      if (req.getState() != RequestState.PENDING) {
        throw new BadRequestException(
          "Cannot delete a request once administration has started processing it"
        );
      }
    }

    requestRepository.delete(req);
  }
}
