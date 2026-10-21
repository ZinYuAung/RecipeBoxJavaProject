package com.recipebox.App.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.recipebox.App.converter.BmiCategoryConverter;
import com.recipebox.App.converter.MealTypeConverter;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.enums.MealType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "recipes")
public class Recipe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer recipeId;

	@NotBlank(message = "Recipe name cannot be blank.")
	@Size(max = 100,message = "Recipe name must not exceed 100 characters.")
	@Pattern(regexp = "^$|.*[a-zA-Zа-яА-ЯğüşöçİĞÜŞÖÇ].*", message = "Recipe name cannot be only numbers or invalid characters.")
	private String name;

	@NotBlank(message = "Write Description")
	@Size(max = 1000, message = "Description must not exceed 1000 characters.")
	private String description;

	@NotNull(message = "Cooking time is required.")
	@Min(value = 1, message = "Cooking time must be at least 1 minute.")
	@Max(value = 1440, message = "Cooking time cannot exceed 24 hours (1440 mins).")
	private Integer cookingTimeMin;

	private String imageUrl;

	@NotNull(message = "Meal type is required.")
	@Convert(converter = MealTypeConverter.class)
	private MealType mealType;

	@NotNull(message = "BMI category is required.")
	@Convert(converter = BmiCategoryConverter.class)
	private BMICategory bmiCategory;
	
	@Min(value = 0, message = "Calories cannot be negative")
	@Column(name = "calories")
    private Integer calories;

	private Boolean isActive = true;

	private LocalDateTime updatedAt;
	
	@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RecipeIngredient> ingredients = new HashSet<>();

	public Set<RecipeIngredient> getIngredients() {
	    return ingredients;
	}

	public void setIngredients(Set<RecipeIngredient> ingredients) {
	    this.ingredients = ingredients;
	}

	public Recipe() { }

	public Recipe(Integer recipeId, String name, String description, Integer cookingTimeMin, String imageUrl,
			MealType mealType, BMICategory bmiCategory, Boolean isActive, LocalDateTime updatedAt) {
		this.recipeId = recipeId;
		this.name = name;
		this.description = description;
		this.cookingTimeMin = cookingTimeMin;
		this.imageUrl = imageUrl;
		this.mealType = mealType;
		this.bmiCategory = bmiCategory;
		this.isActive = isActive;
		this.updatedAt = updatedAt;
	}

	public Recipe(String name, String description, Integer cookingTimeMin, String imageUrl, MealType mealType,
			BMICategory bmiCategory, Boolean isActive, LocalDateTime updatedAt) {
		this.name = name;
		this.description = description;
		this.cookingTimeMin = cookingTimeMin;
		this.imageUrl = imageUrl;
		this.mealType = mealType;
		this.bmiCategory = bmiCategory;
		this.isActive = isActive;
		this.updatedAt = updatedAt;
	}

	public Integer getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(Integer recipeId) {
		this.recipeId = recipeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCookingTimeMin() {
		return cookingTimeMin;
	}

	public void setCookingTimeMin(Integer cookingTimeMin) {
		this.cookingTimeMin = cookingTimeMin;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public MealType getMealType() {
		return mealType;
	}

	public void setMealType(MealType mealType) {
		this.mealType = mealType;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public com.recipebox.App.enums.BMICategory getBmiCategory() {
		return bmiCategory;
	}

	public void setBmiCategory(com.recipebox.App.enums.BMICategory bmiCategory) {
		this.bmiCategory = bmiCategory;
	}

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }


}