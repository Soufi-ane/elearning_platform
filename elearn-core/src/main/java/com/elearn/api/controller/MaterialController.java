package com.elearn.api.controller;

import java.util.List;
import com.elearn.api.entity.AssignmentSubmission;
import com.elearn.api.entity.Schemas.AssignmentSubmissionResponse;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.elearn.api.entity.Material;
import com.elearn.api.entity.User;
import com.elearn.api.entity.Schemas.ElementWorkspaceResponse;
import com.elearn.api.entity.Schemas.FolderCreateRequest;
import com.elearn.api.entity.Schemas.FolderResponse;
import com.elearn.api.entity.Schemas.MaterialResponse;
import com.elearn.api.service.MaterialService;

@RestController
@RequestMapping("/api/v1")
public class MaterialController {
  private final MaterialService materialService;

  public MaterialController(MaterialService materialService) {
    this.materialService = materialService;
  }

  @GetMapping("/elements/{elementId}/workspace")
  public ResponseEntity<ElementWorkspaceResponse> getWorkspace(
    @PathVariable String elementId,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(materialService.getWorkspace(elementId, user));
  }

  @PostMapping("/elements/{elementId}/folders")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<FolderResponse> createFolder(
    @PathVariable String elementId,
    @RequestBody FolderCreateRequest request,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(
      materialService.createFolder(elementId, request, user)
    );
  }

  @DeleteMapping("/elements/{elementId}/folders/{folderId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<Void> deleteFolder(
    @PathVariable String elementId,
    @PathVariable String folderId,
    @AuthenticationPrincipal User user
  ) {
    materialService.deleteFolder(elementId, folderId, user);
    return ResponseEntity.ok().build();
  }

  @PostMapping(
    value = "/elements/{elementId}/materials",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<MaterialResponse> uploadMaterial(
    @PathVariable String elementId,
    @RequestParam(value = "file", required = false) MultipartFile file,
    @RequestParam(value = "title", required = false) String title,
    @RequestParam(value = "description", required = false) String description,
    @RequestParam(value = "folderId", required = false) String folderId,
    @RequestParam(
      value = "assignment",
      defaultValue = "false"
    ) boolean assignment,
    @RequestParam(value = "deadline", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime deadline,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(
      materialService.uploadMaterial(
        elementId,
        file,
        title,
        description,
        folderId,
        assignment,
        deadline,
        user
      )
    );
  }

  @DeleteMapping("/elements/{elementId}/materials/{materialId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<Void> deleteMaterial(
    @PathVariable String elementId,
    @PathVariable String materialId,
    @AuthenticationPrincipal User user
  ) {
    materialService.deleteMaterial(elementId, materialId, user);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/materials/{materialId}/download")
  public ResponseEntity<byte[]> downloadMaterial(
    @PathVariable String materialId,
    @RequestParam(value = "inline", defaultValue = "false") boolean inline,
    @AuthenticationPrincipal User user
  ) {
    Material material = materialService.getMaterialForDownload(
      materialId,
      user
    );
    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    if (material.getContentType() != null) {
      try {
        mediaType = MediaType.parseMediaType(material.getContentType());
      } catch (Exception ignored) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      }
    }
    String fileName = material.getOriginalFileName() != null
      ? material.getOriginalFileName()
      : "download";
    ContentDisposition disposition = inline
      ? ContentDisposition.inline().filename(fileName).build()
      : ContentDisposition.attachment().filename(fileName).build();

    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .body(material.getFileData());
  }

  @PostMapping(
    value = "/materials/{materialId}/submissions",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<AssignmentSubmissionResponse> submitAssignment(
    @PathVariable String materialId,
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "comment", required = false) String comment,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(
      materialService.submitAssignment(materialId, file, comment, user)
    );
  }

  @GetMapping("/materials/{materialId}/submissions")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<List<AssignmentSubmissionResponse>> getSubmissions(
    @PathVariable String materialId,
    @AuthenticationPrincipal User user
  ) {
    return ResponseEntity.ok(
      materialService.getSubmissionsForAssignment(materialId, user)
    );
  }

  @GetMapping("/submissions/{submissionId}/download")
  public ResponseEntity<byte[]> downloadSubmission(
    @PathVariable String submissionId,
    @AuthenticationPrincipal User user
  ) {
    AssignmentSubmission sub = materialService.getSubmissionForDownload(
      submissionId,
      user
    );
    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    if (sub.getContentType() != null) {
      try {
        mediaType = MediaType.parseMediaType(sub.getContentType());
      } catch (Exception ignored) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      }
    }
    String fileName = sub.getOriginalFileName() != null
      ? sub.getOriginalFileName()
      : "submission";
    ContentDisposition disposition = ContentDisposition
      .attachment()
      .filename(fileName)
      .build();

    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .body(sub.getFileData());
  }
}
