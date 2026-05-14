package com.elearn.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.Result;

@Repository
public interface ResultRepository extends JpaRepository<Result, String> {}
