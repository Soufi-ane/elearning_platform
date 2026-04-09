package com.elearn.api.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.DayPlan;

@Repository
public interface DayPlanRepository extends JpaRepository<DayPlan, String> {

  List<DayPlan> findByDateBetween(LocalDate startDate, LocalDate endDate);

}
