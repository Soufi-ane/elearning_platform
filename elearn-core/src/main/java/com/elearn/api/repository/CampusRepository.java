package com.elearn.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Campus;

@Repository
public interface CampusRepository extends JpaRepository<Campus, String> { }
