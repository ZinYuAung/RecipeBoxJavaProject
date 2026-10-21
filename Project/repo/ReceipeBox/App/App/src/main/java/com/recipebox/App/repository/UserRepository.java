package com.recipebox.App.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.recipebox.App.entity.User;
import com.recipebox.App.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findByEmail(String email);
  
  boolean existsByEmail(String email);
  
  boolean existsByName(String name);
  
  List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nameKeyword, String emailKeyword);

  Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nameKeyword, String emailKeyword, Pageable pageable);
  Page<User> findByRoleIn(List<UserRole> roles, Pageable pageable);

  Page<User> findByRoleInAndNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      List<UserRole> roles, String nameKeyword, String emailKeyword, Pageable pageable);
  Page<User> findByRoleInAndUserIdNot(List<UserRole> roles, int userId, Pageable pageable);

  Page<User> findByRoleInAndUserIdNotAndNameContainingIgnoreCaseOrRoleInAndUserIdNotAndEmailContainingIgnoreCase(
      List<UserRole> roles1, int userId1, String nameKeyword, 
      List<UserRole> roles2, int userId2, String emailKeyword, Pageable pageable);
}