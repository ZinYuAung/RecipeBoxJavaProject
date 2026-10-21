package com.recipebox.App.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.recipebox.App.entity.Allergy;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Integer> {
	List<Allergy> findByIsDeletedFalse();

	List<Allergy> findByIsDeletedFalseOrderByNameAsc();

	// Search method for keyword filtering
	List<Allergy> findByNameContainingIgnoreCase(String keyword);

	@Query("SELECT ua.allergen.allergenId FROM UserAllergy ua WHERE ua.user.userId = :userId")
	List<Integer> findSubscribedAllergenIdsByUserId(@Param("userId") Integer userId);

	List<Allergy> findByNameIgnoreCase(String name);

	List<Allergy> findByNameIgnoreCaseAndAllergenIdNot(String name, int allergenId);
}