package com.elearn.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleResponse;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Module;
import com.elearn.api.entity.Role;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.ModuleRepository;

@Service
public class ModuleService {
  private final ModuleRepository moduleRepository;
  private final ElementRepository elementRepository;

  @Autowired
  public ModuleService(ModuleRepository moduleRepository,ElementRepository elementRepository){
    this.moduleRepository = moduleRepository;
    this.elementRepository = elementRepository;
  }

  public List<ModuleResponse> getModulesForUser(User user) {
    List<Module> modules = switch (user.getRole()) {
      case ADMIN -> moduleRepository.findAll();
      case TEACHER -> moduleRepository.findDistinctByElementsTeacherId(user.getId());
      default -> moduleRepository.findByDepartmentAndSemester(
          user.getDepartment(), user.getSemester());
    };
    return modules.stream().map(ModuleResponse::new).toList();
  }

  public List<ElementResponse> getElementsForModule(String moduleId, User user) {
    List<Element> elements = (user.getRole() == Role.TEACHER)
      ? elementRepository.findByModuleIdAndTeacherId(moduleId, user.getId())
      : elementRepository.findByModuleId(moduleId);

    return elements.stream().map(ElementResponse::new).toList();
  }
}

