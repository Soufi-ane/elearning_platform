package com.elearn.api.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Result;

@Repository
public interface ResultRepository
  extends JpaRepository<Result, String>, JpaSpecificationExecutor<Result> {

  List<Result> findByElement_Module_SemesterAndStudent_Id(
    int semester,
    String studentId
  );

  List<Result> findByElement_Id(String elementId);

  Optional<Result> findByStudent_IdAndElement_Id(
    String studentId,
    String elementId
  );

  boolean existsByStudent_IdAndElement_Id(String studentId, String elementId);
}
