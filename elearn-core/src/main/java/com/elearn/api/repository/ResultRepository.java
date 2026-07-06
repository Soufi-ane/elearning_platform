package com.elearn.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Result;

@Repository
public interface ResultRepository extends JpaRepository<Result, String> {

  public List<Result> findByElement_Module_SemesterAndStudent_Id(int semester, String studentId);

}
