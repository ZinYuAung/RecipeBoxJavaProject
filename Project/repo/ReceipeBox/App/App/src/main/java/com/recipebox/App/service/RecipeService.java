package com.recipebox.App.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.recipebox.App.entity.Recipe;

public interface RecipeService {
	
	List<Recipe> getAllRecipes();
	
	List<Recipe> getActiveRecipes();

	Recipe getById(Integer id);

	void save(Recipe recipe);

	void save(Recipe recipe, List<Integer> ingredientIds);

	void save(Recipe recipe, List<Integer> ingredientIds, List<BigDecimal> quantities, List<String> units);

	void delete(Integer id);
	
	void activate(Integer id);
	
	void loadEditFormAttributes(Integer recipeId, Model model);

	String saveImageFile(MultipartFile imageFile) throws IOException;
}
