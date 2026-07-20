package com.elearn.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Absence;
import com.elearn.api.entity.DepartmentName;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, String> {

  List<Absence> findByStudentId(String id);

  List<Absence> findByElementIdAndStudentId(String elementId, String studentId);

  List<Absence> findByStudentDepartment(DepartmentName department);

  @Query(
    "SELECT a FROM Absence a WHERE LOWER(a.student.firstName) LIKE " +
    "LOWER(CONCAT('%', :name, '%')) OR LOWER(a.student.lastName) LIKE LOWER(CONCAT('%', :name, '%'))"
  )
  List<Absence> searchByStudentName(@Param("name") String name);

  @Query("SELECT a FROM Absence a WHERE a.element.teacher.id = :teacherId")
  List<Absence> findByTeacherId(@Param("teacherId") String teacherId);

  boolean existsByStudentIdAndElementIdAndDateTime(
    String studentId,
    String elementId,
    LocalDateTime dateTime
  );

  Absence findByStudentIdAndElementIdAndDateTime(
    String studentId, 
    String elementId, 
    LocalDateTime dateTime
  );

}
