package com.recipebox.App.service;

import java.util.List;
import com.recipebox.App.entity.Ingredient;

public interface IngredientService {

	List<Ingredient> getAllIngredients();

	List<Ingredient> getActiveIngredients();
	
	Ingredient getById(Integer id);

	void save(Ingredient ingredient);

	void delete(Integer id);

	void save(Ingredient ingredient, List<Integer> allergenIds);
	
	void activate(Integer id);
	List<Ingredient> getAllIngredients(String keyword);
}