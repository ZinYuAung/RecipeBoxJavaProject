package com.recipebox.App.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.recipebox.App.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

	// Fetch active ingredients (soft-delete respected)
	List<Ingredient> findByIsDeletedFalseOrderByNameAsc();

	// Fetch active ingredients filtered by name keyword
	List<Ingredient> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword);

	List<Ingredient> findByNameIgnoreCase(String name);
	
	List<Ingredient> findByIsDeletedFalse();

	List<Ingredient> findByNameIgnoreCaseAndIngredientIdNot(String name, Integer ingredientId);

}