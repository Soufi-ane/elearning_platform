package com.elearn.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Absence;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, String> {

  List<Absence> findByStudentId(String id);

}
