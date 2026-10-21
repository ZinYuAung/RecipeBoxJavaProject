package com.recipebox.App.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipebox.App.entity.MealPlan;

@Repository
public interface MealPlanRepository  extends JpaRepository<MealPlan, Integer> {

	List<MealPlan> findByUserUserIdOrderByCreatedAtDesc(int userId);
	
	@Modifying(clearAutomatically = true)
    @Query("UPDATE MealPlan m SET m.isActive = false WHERE m.user.userId = :userId")
    void deactivateAllPlansForUser(@Param("userId") Integer userId);

	List<MealPlan> findByUserUserIdOrderByIsActiveDescCreatedAtDesc(int userId);

	Optional<MealPlan> findByPlanIdAndUserUserId(int planId, int userId);

}
