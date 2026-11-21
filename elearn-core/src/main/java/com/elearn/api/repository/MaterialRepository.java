package com.elearn.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.elearn.api.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, String> {

  List<Material> findByElementIdOrderByCreatedAtDesc(String elementId);

}
