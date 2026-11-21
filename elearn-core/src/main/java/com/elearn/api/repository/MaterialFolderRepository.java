package com.elearn.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.elearn.api.entity.MaterialFolder;

public interface MaterialFolderRepository
    extends JpaRepository<MaterialFolder, String> {
  List<MaterialFolder> findByElementIdOrderByNameAsc(String elementId);
}
