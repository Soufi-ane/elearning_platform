package com.elearn.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.elearn.api.entity.Schemas.RoomBaseResponse;
import com.elearn.api.repository.RoomRepository;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
  private final RoomRepository roomRepository;

  @Autowired
  public RoomController(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public List<RoomBaseResponse> getAllRooms() {
    return roomRepository.findAll().stream().map(RoomBaseResponse::new).toList();
  }
}
