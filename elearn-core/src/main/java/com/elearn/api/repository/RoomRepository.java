package com.elearn.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> { }
