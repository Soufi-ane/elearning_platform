package com.elearn.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

  List<Request> findByStudentIdOrderByRequestDateDesc(String studentId);

  List<Request> findAllByOrderByRequestDateDesc();

}
