package com.elearn.api.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.elearn.api.entity.AssignmentSubmission;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, String> {
   
  Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(
    String assignmentId,
    String studentId
  );

  List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtDesc(
    String assignmentId
  );

  List<AssignmentSubmission> findByAssignmentElementIdAndStudentId(
    String elementId,
    String studentId
  );

}
