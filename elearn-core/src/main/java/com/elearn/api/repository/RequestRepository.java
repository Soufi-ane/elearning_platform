package com.elearn.api.repository;

import com.elearn.api.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, String> {
  List<Request> findByStudentId(String studentId);
}
