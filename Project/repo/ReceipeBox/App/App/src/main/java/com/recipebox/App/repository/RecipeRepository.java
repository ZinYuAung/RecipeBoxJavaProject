package com.recipebox.App.repository;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipebox.App.entity.Recipe;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.enums.MealType;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

	// Active Recipe
	List<Recipe> findByIsActiveTrue();

	// Search by MealType
	List<Recipe> findByMealTypeAndIsActiveTrue(MealType mealType);

	@Query("SELECT DISTINCT r FROM Recipe r " + "LEFT JOIN FETCH r.ingredients ri " + "LEFT JOIN FETCH ri.ingredient i "
			+ "WHERE r.bmiCategory = :bmiCat AND r.isActive = true")
	List<Recipe> findByBmiCategoryAndIsActiveTrue(@Param("bmiCat") BMICategory bmiCat);

	@Query("SELECT DISTINCT r FROM Recipe r " + "LEFT JOIN FETCH r.ingredients ri " + "LEFT JOIN FETCH ri.ingredient i "
			+ "WHERE r.isActive = true")
	List<Recipe> findAllByIsActiveTrue();

	@Nullable
	List<Recipe> findTop4ByOrderByRecipeIdDesc();

	@Query("SELECT DISTINCT r FROM Recipe r " + "LEFT JOIN r.ingredients ri " + "LEFT JOIN ri.ingredient i "
			+ "WHERE (LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "   OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND r.isActive = true")
	List<Recipe> searchByKeywordInRecipeOrIngredient(@Param("keyword") String keyword);

	List<Recipe> findByNameIgnoreCase(String name);

	List<Recipe> findByNameIgnoreCaseAndRecipeIdNot(String name, Integer recipeId);

	@Query("""
		    SELECT DISTINCT r FROM Recipe r
		    LEFT JOIN r.ingredients ri
		    LEFT JOIN ri.ingredient ing
		    LEFT JOIN ing.ingredientAllergies ia
		    WHERE r.isActive = true
		      AND (
		          r.bmiCategory = :targetCategory 
		          OR (r.calories >= :minCalories AND r.calories <= :maxCalories)
		      )
		      AND (
		          :allergenIds IS NULL OR 
		          ri IS NULL OR 
		          ia IS NULL OR 
		          ia.allergy.allergenId NOT IN :allergenIds
		      )
		""")
		List<Recipe> findSuggestedRecipes(
		        @Param("targetCategory") BMICategory targetCategory,
		        @Param("minCalories") Integer minCalories,
		        @Param("maxCalories") Integer maxCalories,
		        @Param("allergenIds") List<Integer> allergenIds
		);
}