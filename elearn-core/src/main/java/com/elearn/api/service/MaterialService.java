package com.elearn.api.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.elearn.api.entity.AssignmentSubmission;
import com.elearn.api.entity.Element;
import com.elearn.api.entity.FileType;
import com.elearn.api.entity.Material;
import com.elearn.api.entity.MaterialFolder;
import com.elearn.api.entity.Role;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.AssignmentSubmissionResponse;
import com.elearn.api.entity.Schemas.ElementResponse;
import com.elearn.api.entity.Schemas.ElementWorkspaceResponse;
import com.elearn.api.entity.Schemas.FolderCreateRequest;
import com.elearn.api.entity.Schemas.FolderResponse;
import com.elearn.api.entity.Schemas.MaterialResponse;
import com.elearn.api.repository.AssignmentSubmissionRepository;
import com.elearn.api.repository.ElementRepository;
import com.elearn.api.repository.MaterialFolderRepository;
import com.elearn.api.repository.MaterialRepository;

@Service
public class MaterialService {
  private static final long MAX_LATE_HOURS = 24L;

  private final ElementRepository elementRepository;
  private final MaterialFolderRepository folderRepository;
  private final MaterialRepository materialRepository;
  private final AssignmentSubmissionRepository submissionRepository;
  private final ModuleService moduleService;

  public MaterialService(
    ElementRepository elementRepository,
    MaterialFolderRepository folderRepository,
    MaterialRepository materialRepository,
    AssignmentSubmissionRepository submissionRepository,
    ModuleService moduleService
  ) {
    this.elementRepository = elementRepository;
    this.folderRepository = folderRepository;
    this.materialRepository = materialRepository;
    this.submissionRepository = submissionRepository;
    this.moduleService = moduleService;
  }

  private Element getElementOrThrow(String elementId) {
    return elementRepository.findById(elementId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private boolean canManage(User user, Element element) {
    if (user.getRole() == Role.ADMIN) {
      return true;
    }
    return user.getRole() == Role.TEACHER &&
      element.getTeacher() != null &&
      element.getTeacher().getId().equals(user.getId());
  }

  private void assertCanView(User user, Element element) {
    if (user.getRole() == Role.ADMIN) {
      return;
    }
    if (user.getRole() == Role.TEACHER) {
      if (!canManage(user, element)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
      return;
    }
    if (element.getModule() == null ||
        !moduleService.canStudentAccessModule(user, element.getModule())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  private void assertCanManage(User user, Element element) {
    if (!canManage(user, element)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  public ElementWorkspaceResponse getWorkspace(String elementId, User user) {
    Element element = getElementOrThrow(elementId);
    assertCanView(user, element);

    List<FolderResponse> folders = folderRepository
      .findByElementIdOrderByNameAsc(elementId)
      .stream()
      .map(FolderResponse::new)
      .toList();

    List<MaterialResponse> materials = materialRepository
      .findByElementIdOrderByCreatedAtDesc(elementId)
      .stream()
      .map(MaterialResponse::new)
      .toList();

    List<AssignmentSubmissionResponse> mySubmissions =
      user.getRole() == Role.STUDENT
        ? submissionRepository
            .findByAssignmentElementIdAndStudentId(elementId, user.getId())
            .stream()
            .map(AssignmentSubmissionResponse::new)
            .toList()
        : List.of();

    return new ElementWorkspaceResponse(
      new ElementResponse(element),
      canManage(user, element),
      folders,
      materials,
      mySubmissions
    );
  }

  public FolderResponse createFolder(
    String elementId,
    FolderCreateRequest request,
    User user
  ) {
    Element element = getElementOrThrow(elementId);
    assertCanManage(user, element);

    if (request.name() == null || request.name().trim().isEmpty()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Folder name is required"
      );
    }

    MaterialFolder parent = null;
    if (request.parentId() != null && !request.parentId().isBlank()) {
      parent = folderRepository.findById(request.parentId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      if (!parent.getElement().getId().equals(elementId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    }

    MaterialFolder folder = new MaterialFolder(
      request.name().trim(),
      element,
      parent
    );
    return new FolderResponse(folderRepository.save(folder));
  }

  @Transactional
  public void deleteFolder(String elementId, String folderId, User user) {
    Element element = getElementOrThrow(elementId);
    assertCanManage(user, element);

    MaterialFolder folder = folderRepository.findById(folderId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!folder.getElement().getId().equals(elementId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    folderRepository.delete(folder);
  }

  public MaterialResponse uploadMaterial(
    String elementId,
    MultipartFile file,
    String title,
    String description,
    String folderId,
    boolean assignment,
    LocalDateTime deadline,
    User user
  ) {
    Element element = getElementOrThrow(elementId);
    assertCanManage(user, element);

    MaterialFolder folder = null;
    if (folderId != null && !folderId.isBlank()) {
      folder = folderRepository.findById(folderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      if (!folder.getElement().getId().equals(elementId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    }

    if ((file == null || file.isEmpty()) && !assignment) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "File is required"
      );
    }

    Material material = new Material();
    material.setElement(element);
    material.setFolder(folder);
    material.setUploadedBy(user);
    material.setAssignment(assignment);
    material.setDeadline(deadline);
    material.setDescription(description);
    material.setCreatedAt(LocalDateTime.now());

    if (file != null && !file.isEmpty()) {
      String origName = file.getOriginalFilename() != null
        ? file.getOriginalFilename()
        : "file";
      String contentType = file.getContentType() != null
        ? file.getContentType()
        : "application/octet-stream";
      try {
        material.setFileData(file.getBytes());
      } catch (IOException ex) {
        throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to read uploaded file"
        );
      }
      material.setOriginalFileName(origName);
      material.setContentType(contentType);
      material.setFileSize(file.getSize());
      material.setFileType(resolveFileType(origName, contentType));
      material.setTitle(
        (title != null && !title.isBlank()) ? title.trim() : origName
      );
    } else {
      if (title == null || title.isBlank()) {
        throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Assignment title is required"
        );
      }
      material.setTitle(title.trim());
      material.setFileType(FileType.OTHER);
      material.setFileSize(0L);
    }

    return new MaterialResponse(materialRepository.save(material));
  }

  @Transactional
  public void deleteMaterial(String elementId, String materialId, User user) {
    Element element = getElementOrThrow(elementId);
    assertCanManage(user, element);

    Material material = materialRepository.findById(materialId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!material.getElement().getId().equals(elementId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    materialRepository.delete(material);
  }

  public Material getMaterialForDownload(String materialId, User user) {
    Material material = materialRepository.findById(materialId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    assertCanView(user, material.getElement());
    if (material.getFileData() == null || material.getFileData().length == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return material;
  }

  @Transactional
  public AssignmentSubmissionResponse submitAssignment(
    String materialId,
    MultipartFile file,
    String comment,
    User user
  ) {
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Submission file is required"
      );
    }

    Material assignment = materialRepository.findById(materialId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!assignment.isAssignment()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Target material is not an assignment"
      );
    }
    assertCanView(user, assignment.getElement());

    LocalDateTime now = LocalDateTime.now();
    boolean late = false;
    if (assignment.getDeadline() != null) {
      LocalDateTime cutoff = assignment.getDeadline().plusHours(MAX_LATE_HOURS);
      if (now.isAfter(cutoff)) {
        throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Submission closed: 24h late window has expired"
        );
      }
      late = now.isAfter(assignment.getDeadline());
    }

    AssignmentSubmission sub = submissionRepository
      .findByAssignmentIdAndStudentId(materialId, user.getId())
      .orElseGet(AssignmentSubmission::new);

    String origName = file.getOriginalFilename() != null
      ? file.getOriginalFilename()
      : "submission";
    String contentType = file.getContentType() != null
      ? file.getContentType()
      : "application/octet-stream";

    try {
      sub.setFileData(file.getBytes());
    } catch (IOException ex) {
      throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to read submission file"
      );
    }

    sub.setAssignment(assignment);
    sub.setStudent(user);
    sub.setOriginalFileName(origName);
    sub.setContentType(contentType);
    sub.setFileSize(file.getSize());
    sub.setSubmittedAt(now);
    sub.setLate(late);
    sub.setComment(comment != null ? comment.trim() : null);

    return new AssignmentSubmissionResponse(submissionRepository.save(sub));
  }

  public List<AssignmentSubmissionResponse> getSubmissionsForAssignment(
    String materialId,
    User user
  ) {
    Material assignment = materialRepository.findById(materialId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!assignment.isAssignment()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    assertCanManage(user, assignment.getElement());

    return submissionRepository
      .findByAssignmentIdOrderBySubmittedAtDesc(materialId)
      .stream()
      .map(AssignmentSubmissionResponse::new)
      .toList();
  }

  public AssignmentSubmission getSubmissionForDownload(
    String submissionId,
    User user
  ) {
    AssignmentSubmission sub = submissionRepository.findById(submissionId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    boolean isOwner = sub.getStudent() != null &&
      sub.getStudent().getId().equals(user.getId());
    boolean isManager = canManage(user, sub.getAssignment().getElement());

    if (!isOwner && !isManager) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (sub.getFileData() == null || sub.getFileData().length == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return sub;
  }

  private FileType resolveFileType(String fileName, String contentType) {
    String lower = fileName.toLowerCase();
    String ct = contentType.toLowerCase();
    if (ct.startsWith("image/") || lower.endsWith(".png") ||
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
        lower.endsWith(".webp") || lower.endsWith(".gif") ||
        lower.endsWith(".svg")) {
      return FileType.IMAGE;
    }
    if (lower.endsWith(".pdf")) {
      return FileType.PDF;
    }
    if (lower.endsWith(".docx") || lower.endsWith(".doc")) {
      return FileType.DOCX;
    }
    if (lower.endsWith(".pptx") || lower.endsWith(".ppt")) {
      return FileType.PPTX;
    }
    if (lower.endsWith(".txt") || lower.endsWith(".md")) {
      return FileType.TXT;
    }
    if (lower.endsWith(".mp4") || lower.endsWith(".webm")) {
      return FileType.MP4;
    }
    if (lower.endsWith(".zip") || lower.endsWith(".tar") ||
        lower.endsWith(".gz") || lower.endsWith(".rar")) {
      return FileType.ZIP;
    }
    if (lower.endsWith(".java") || lower.endsWith(".c") ||
        lower.endsWith(".cpp") || lower.endsWith(".py") ||
        lower.endsWith(".ts") || lower.endsWith(".js") ||
        lower.endsWith(".sql") || lower.endsWith(".html") ||
        lower.endsWith(".css")) {
      return FileType.SOURCE_CODE;
    }
    return FileType.OTHER;
  }
}
