package com.elearn.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleResponse;
import com.elearn.api.entity.Schemas.ModuleRequest;
import com.elearn.api.entity.Schemas.ElementRequest;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Module;
import com.elearn.api.entity.Role;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.ModuleRepository;
import com.elearn.api.repository.UserRepository;

@Service
public class ModuleService {
  private final ModuleRepository moduleRepository;
  private final ElementRepository elementRepository;
  private final UserRepository userRepository;

  @Autowired
  public ModuleService(
    ModuleRepository moduleRepository,
    ElementRepository elementRepository,
    UserRepository userRepository
  ) {
    this.moduleRepository = moduleRepository;
    this.elementRepository = elementRepository;
    this.userRepository = userRepository;
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

  public ModuleResponse createModule(ModuleRequest request) {
    Module module = new Module(
      request.name(), request.semester(), request.department()
    );
    return new ModuleResponse(moduleRepository.save(module));
  }

  public ModuleResponse updateModule(String id, ModuleRequest request) {
    Module module = moduleRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    module.setName(request.name());
    module.setSemester(request.semester());
    module.setDepartment(request.department());
    return new ModuleResponse(moduleRepository.save(module));
  }

  public void deleteModule(String id) {
    moduleRepository.deleteById(id);
  }

  public ElementResponse createElement(ElementRequest request) {
    Module module = moduleRepository.findById(request.moduleId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    User teacher = userRepository.findById(request.teacherId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    
    Element element = new Element(request.name(), module, teacher);
    return new ElementResponse(elementRepository.save(element));
  }

  public ElementResponse updateElement(String id, ElementRequest request) {
    Element element = elementRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    Module module = moduleRepository.findById(request.moduleId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    User teacher = userRepository.findById(request.teacherId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    
    element.setName(request.name());
    element.setModule(module);
    element.setTeacher(teacher);
    return new ElementResponse(elementRepository.save(element));
  }

  public void deleteElement(String id) {
    elementRepository.deleteById(id);
  }
}
