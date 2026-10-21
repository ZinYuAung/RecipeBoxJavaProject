package com.recipebox.App.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipebox.App.entity.Allergy;
import com.recipebox.App.entity.User;
import com.recipebox.App.entity.UserAllergy;

@Repository
public interface UserAllergyRepository extends JpaRepository<UserAllergy, Integer> {
	
	List<UserAllergy> findByUser(User user);
	
    boolean existsByUserAndAllergen(User user, Allergy allergy);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserAllergy ua WHERE ua.user = :user")
    void deleteByUserCustom(@Param("user") User user);
}
