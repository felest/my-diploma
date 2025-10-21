package org.example.diploma.repository;

import org.example.diploma.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByGroupId(Long groupId);
    List<User> findByRoleAndGroupIsNull(String role);
    boolean existsByUsername(String username);
}