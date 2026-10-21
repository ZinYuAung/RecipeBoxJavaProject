package com.recipebox.App.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipebox.App.entity.Ingredient;
import com.recipebox.App.entity.IngredientAllergy;
import com.recipebox.App.entity.MealPlan;
import com.recipebox.App.entity.MealPlanItem;
import com.recipebox.App.entity.Recipe;
import com.recipebox.App.entity.RecipeIngredient;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.enums.MealType;
import com.recipebox.App.repository.MealPlanRepository;
import com.recipebox.App.repository.RecipeRepository;
import com.recipebox.App.service.MealPlanService;

@Service
public class MealPlanServiceImpl implements MealPlanService {

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private MealPlanRepository mealPlanRepository;
	
	@Override
	@Transactional(readOnly = true)
	public List<MealPlanItem> generate7DayMealPlan(String categoryString, List<Integer> userAllergenIds) {
	    BMICategory bmiCat = BMICategory.fromString(categoryString);
	    int minMealCal = bmiCat.getMinMealCalories();
	    int maxMealCal = bmiCat.getMaxMealCalories();

	    List<Integer> safeAllergenIds = (userAllergenIds == null || userAllergenIds.isEmpty()) 
	                                    ? List.of(-1) 
	                                    : userAllergenIds;

	    List<Recipe> candidateRecipes = recipeRepository.findSuggestedRecipes(
	            bmiCat, minMealCal, maxMealCal, safeAllergenIds
	    );

	    if (candidateRecipes.isEmpty()) {
	        candidateRecipes = recipeRepository.findAllByIsActiveTrue();
	    }
	    List<Recipe> safeRecipes = candidateRecipes.stream()
	            .filter(recipe -> isRecipeSafeForUser(recipe, userAllergenIds))
	            .collect(Collectors.toList());

	    if (safeRecipes.isEmpty()) {
	        return new ArrayList<>();
	    }

	    List<Recipe> breakfasts = safeRecipes.stream()
	            .filter(r -> r.getMealType() == MealType.BREAKFAST).toList();
	    List<Recipe> lunches = safeRecipes.stream()
	            .filter(r -> r.getMealType() == MealType.LUNCH).toList();
	    List<Recipe> dinners = safeRecipes.stream()
	            .filter(r -> r.getMealType() == MealType.DINNER).toList();

	    List<MealPlanItem> generatedItems = new ArrayList<>();
	    Random rand = new Random();
	    for (int day = 1; day <= 7; day++) {
	        
	        Recipe breakfastRecipe = !breakfasts.isEmpty() 
	                ? breakfasts.get(rand.nextInt(breakfasts.size())) 
	                : safeRecipes.get(rand.nextInt(safeRecipes.size()));
	        generatedItems.add(createMealItem(day, MealType.BREAKFAST, breakfastRecipe));
	        Recipe lunchRecipe = !lunches.isEmpty() 
	                ? lunches.get(rand.nextInt(lunches.size())) 
	                : safeRecipes.get(rand.nextInt(safeRecipes.size()));
	        generatedItems.add(createMealItem(day, MealType.LUNCH, lunchRecipe));
	        Recipe dinnerRecipe = !dinners.isEmpty() 
	                ? dinners.get(rand.nextInt(dinners.size())) 
	                : safeRecipes.get(rand.nextInt(safeRecipes.size()));
	        generatedItems.add(createMealItem(day, MealType.DINNER, dinnerRecipe));
	    }

	    return generatedItems;
	}
	
	private MealPlanItem createMealItem(int day, MealType type, Recipe recipe) {
	    MealPlanItem item = new MealPlanItem();
	    item.setDayNumber(day);
	    item.setMealType(type);
	    item.setRecipe(recipe);
	    return item;
	}

	private boolean isRecipeSafeForUser(Recipe recipe, List<Integer> userAllergenIds) {
	    if (userAllergenIds == null || userAllergenIds.isEmpty()) {
	        return true;
	    }
	    if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
	        return true;
	    }

	    for (RecipeIngredient ri : recipe.getIngredients()) {
	        Ingredient ingredient = ri.getIngredient();
	        if (ingredient != null && ingredient.getIngredientAllergies() != null) {
	            for (IngredientAllergy ia : ingredient.getIngredientAllergies()) {
	                if (ia.getAllergy() != null) {
	                    Integer allergenId = ia.getAllergy().getAllergenId();
	                    if (allergenId != null && userAllergenIds.contains(allergenId)) {
	                        return false;
	                    }
	                }
	            }
	        }
	    }
	    return true;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<MealPlan> getMealPlanForUser(Integer planId, Integer userId) {
		return mealPlanRepository.findById(planId).filter(plan -> plan.getUser().getUserId() == userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MealPlanItem> getItemsForDay(Integer planId, Integer userId, Integer dayNum) {
		return getMealPlanForUser(planId, userId).map(plan -> plan.getItems().stream()
				.filter(item -> item.getDayNumber().equals(dayNum)).collect(Collectors.toList())).orElse(List.of());
	}
	
	@Transactional
	public void deleteMealPlan(int planId, int userId) {
	    MealPlan plan = mealPlanRepository.findByPlanIdAndUserUserId(planId, userId)
	            .orElseThrow(() -> new IllegalArgumentException("Meal plan not found or access denied."));

	    mealPlanRepository.delete(plan);
	}

}
