package com.recipebox.App.entity;

import com.recipebox.App.converter.MealTypeConverter;
import com.recipebox.App.enums.MealType;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="meal_plan_items")
public class MealPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ManyToOne
    @JoinColumn(name="plan_id")
    private MealPlan mealPlan;

    @ManyToOne
    @JoinColumn(name="recipe_id")
    private Recipe recipe;
    
    @Convert(converter = MealTypeConverter.class)
    private MealType mealType;

    private Integer dayNumber;

	public MealPlanItem() { }

	public MealPlanItem(Integer itemId, MealPlan mealPlan, Recipe recipe, MealType mealType, Integer dayNumber) {
		this.itemId = itemId;
		this.mealPlan = mealPlan;
		this.recipe = recipe;
		this.mealType = mealType;
		this.dayNumber = dayNumber;
	}
	
	public MealPlanItem(MealPlan mealPlan, Recipe recipe, MealType mealType, Integer dayNumber) {
		this.mealPlan = mealPlan;
		this.recipe = recipe;
		this.mealType = mealType;
		this.dayNumber = dayNumber;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public MealPlan getMealPlan() {
		return mealPlan;
	}

	public void setMealPlan(MealPlan mealPlan) {
		this.mealPlan = mealPlan;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public MealType getMealType() {
		return mealType;
	}

	public void setMealType(MealType mealType) {
		this.mealType = mealType;
	}

	public Integer getDayNumber() {
		return dayNumber;
	}

	public void setDayNumber(Integer dayNumber) {
		this.dayNumber = dayNumber;
	}
}