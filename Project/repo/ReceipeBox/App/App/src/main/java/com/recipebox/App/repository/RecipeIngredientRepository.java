package com.recipebox.App.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.recipebox.App.entity.RecipeIngredient;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Integer> {
	List<RecipeIngredient> findByRecipe_RecipeId(Integer recipeId);

	void deleteByRecipe_RecipeId(Integer recipeId);
	
	boolean existsByIngredient_IngredientIdAndRecipe_IsActiveTrue(Integer ingredientId);
}