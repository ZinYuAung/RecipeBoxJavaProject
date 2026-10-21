package com.recipebox.App.service;

import java.util.List;
import java.util.Optional;

import com.recipebox.App.entity.MealPlan;
import com.recipebox.App.entity.MealPlanItem;

public interface MealPlanService {
	
//	List<MealPlanItem> generate7DayMealPlan(String categoryString);
	List<MealPlanItem> generate7DayMealPlan(String categoryString, List<Integer> userAllergenIds);
	
	Optional<MealPlan> getMealPlanForUser(Integer planId, Integer userId);
	
	List<MealPlanItem> getItemsForDay(Integer planId, Integer userId, Integer dayNum);

	void deleteMealPlan(int planId, int userId);

}
