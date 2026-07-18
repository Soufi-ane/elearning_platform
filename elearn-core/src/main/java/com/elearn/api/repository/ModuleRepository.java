package com.elearn.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module, String> {

    List<Module> findByDepartmentAndSemester(DepartmentName department, int semester);
    List<Module> findDistinctByElementsTeacherId(String teacherId);

}
