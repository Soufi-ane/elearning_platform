package com.elearn.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.elearn.api.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  public Optional<User> findByUsernameOrEmail(String username, String email);
  
}
