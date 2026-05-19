package com.jwxt.repository;

import com.jwxt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jwxt.entity.Role;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRole(Role role);
}
