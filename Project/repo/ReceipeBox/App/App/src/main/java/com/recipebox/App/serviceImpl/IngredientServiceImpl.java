package com.recipebox.App.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipebox.App.entity.Allergy;
import com.recipebox.App.entity.Ingredient;
import com.recipebox.App.entity.IngredientAllergy;
import com.recipebox.App.repository.AllergyRepository;
import com.recipebox.App.repository.IngredientAllergyRepository;
import com.recipebox.App.repository.IngredientRepository;
import com.recipebox.App.repository.RecipeIngredientRepository;
import com.recipebox.App.service.IngredientService;

@Service
public class IngredientServiceImpl implements IngredientService {

	@Autowired
	private IngredientRepository ingredientRepository;

	@Autowired
	private IngredientAllergyRepository ingredientAllergyRepository;

	@Autowired
	private AllergyRepository allergyRepository;
	
	@Autowired
	private RecipeIngredientRepository recipeIngredientRepository;
	
	@Override
	public List<Ingredient> getAllIngredients() {
		return ingredientRepository.findAll();
	}

	@Override
	public List<Ingredient> getAllIngredients(String keyword) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			return ingredientRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword.trim());
		}
		return ingredientRepository.findByIsDeletedFalse();
	}

	@Override
	public Ingredient getById(Integer id) {
		return ingredientRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + id));
	}

	@Override
	public void save(Ingredient ingredient) {
		// sve only one para, not include allergy
		save(ingredient, null);
	}

	@Override
	@Transactional
	public void save(Ingredient ingredient, List<Integer> allergenIds) {
		if (ingredient.getName() != null) {
			ingredient.setName(ingredient.getName().trim());
		}
		if (ingredient.getIngredientId() == null) {
			List<Ingredient> existingList = ingredientRepository.findByNameIgnoreCase(ingredient.getName());
			if (!existingList.isEmpty()) {
				throw new IllegalArgumentException("Ingredient name already exists!");
			}
			ingredient.setCreatedAt(LocalDateTime.now());
			ingredient.setIsDeleted(false);
		} else {
			List<Ingredient> duplicateCheck = ingredientRepository.findByNameIgnoreCaseAndIngredientIdNot(ingredient.getName(), ingredient.getIngredientId());
			if (!duplicateCheck.isEmpty()) {
				throw new IllegalArgumentException("Ingredient name already exists!");
			}
			Ingredient existing = ingredientRepository.findById(ingredient.getIngredientId())
					.orElseThrow(() -> new RuntimeException("Cannot update, ingredient not found"));
			
			ingredient.setCreatedAt(existing.getCreatedAt());
		
			ingredient.setIsDeleted(existing.getIsDeleted());
		}
		ingredientRepository.save(ingredient);

		ingredientAllergyRepository.deleteByIngredient_IngredientId(ingredient.getIngredientId());

		if (allergenIds != null && !allergenIds.isEmpty()) {
			for (Integer allergyId : allergenIds) {
				Allergy allergy = allergyRepository.findById(allergyId)
						.orElseThrow(() -> new RuntimeException("Allergy not found with id: " + allergyId));

				IngredientAllergy ia = new IngredientAllergy(null, ingredient, allergy);
				ingredientAllergyRepository.save(ia);
			}
		}
	}

	@Override
	@Transactional
	public void delete(Integer id) {
		
		Ingredient ingredient = ingredientRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + id));
		 //check Ingredient in active recipe 
		 boolean isUsedInActiveRecipe = recipeIngredientRepository.existsByIngredient_IngredientIdAndRecipe_IsActiveTrue(id);
		 if (isUsedInActiveRecipe) {
		 throw new IllegalStateException("This ingredient is currently used in active recipes and cannot be deleted.");
		 }

		ingredient.setIsDeleted(true);
		ingredientRepository.save(ingredient);
	}

	@Override
	@Transactional
	public void activate(Integer id) {
		Ingredient ingredient = ingredientRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + id));
		ingredient.setIsDeleted(false);
		ingredientRepository.save(ingredient);
	}

	@Override
	public List<Ingredient> getActiveIngredients() {
		// TODO Auto-generated method stub
		return ingredientRepository.findByIsDeletedFalseOrderByNameAsc();
	}
}