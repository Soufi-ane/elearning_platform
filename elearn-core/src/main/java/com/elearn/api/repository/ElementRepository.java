package com.elearn.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Element;

@Repository
public interface ElementRepository extends JpaRepository<Element, String> { }
