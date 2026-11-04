package com.elearn.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.DepartmentName;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.Module;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementRequest;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ModuleRequest;
import com.elearn.api.entity.Schemas.ModuleResponse;
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

  public List<ModuleResponse> getModulesForUser(
    User user,
    DepartmentName department,
    Integer year,
    Integer semester
  ) {
    List<Module> modules = switch (user.getRole()) {
      case ADMIN, TEACHER -> moduleRepository.findAll();
      default -> moduleRepository.findByDepartment(user.getDepartment());
    };

    return modules.stream()
      .filter(m -> department == null || m.getDepartment() == department)
      .filter(m -> year == null || year <= 0 || m.getYear() == year)
      .filter(
        m -> semester == null || semester <= 0 || m.getSemester() == semester
      )
      .map(ModuleResponse::new)
      .toList();
  }

  public List<ElementResponse> getElementsForModule(
    String moduleId,
    User user
  ) {
    List<Element> elements = (user.getRole() == Role.TEACHER)
      ? elementRepository.findByModuleIdAndTeacherId(moduleId, user.getId())
      : elementRepository.findByModuleId(moduleId);
    return elements.stream().map(ElementResponse::new).toList();
  }

  public List<ElementResponse> getAllElements(User user) {
    return elementRepository.findAll().stream()
      .filter(
        e -> user.getRole() == Role.ADMIN ||
          (e.getTeacher() != null &&
           e.getTeacher().getId().equals(user.getId()))
      )
      .map(ElementResponse::new)
      .toList();
  }

  public ModuleResponse createModule(ModuleRequest request) {
    int year = (request.year() != null && request.year() > 0)
      ? request.year()
      : 1;
    Module module = new Module(
      request.name(),
      year,
      request.semester(),
      request.department()
    );
    return new ModuleResponse(moduleRepository.save(module));
  }

  public ModuleResponse updateModule(String id, ModuleRequest request) {
    Module module = moduleRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    int year = (request.year() != null && request.year() > 0)
      ? request.year()
      : Math.max(1, module.getYear());
    module.setName(request.name());
    module.setYear(year);
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
