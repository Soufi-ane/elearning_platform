package com.elearn.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module, String> { }
