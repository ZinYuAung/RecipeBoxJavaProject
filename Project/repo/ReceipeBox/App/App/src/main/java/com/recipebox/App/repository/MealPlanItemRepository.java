package com.recipebox.App.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipebox.App.entity.MealPlanItem;

@Repository
public interface MealPlanItemRepository extends JpaRepository<MealPlanItem, Integer>{
	boolean existsByRecipe_RecipeId(Integer recipeId);
}
