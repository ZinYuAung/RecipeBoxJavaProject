package com.recipebox.App.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.recipebox.App.entity.Ingredient;
import com.recipebox.App.entity.Recipe;
import com.recipebox.App.entity.RecipeIngredient;
import com.recipebox.App.enums.IngredientUnit;
import com.recipebox.App.repository.IngredientRepository;
import com.recipebox.App.repository.MealPlanItemRepository;
import com.recipebox.App.repository.RecipeIngredientRepository;
import com.recipebox.App.repository.RecipeRepository;
import com.recipebox.App.service.RecipeService;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RecipeServiceImpl implements RecipeService {
	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private RecipeIngredientRepository recipeIngredientRepository;

	@Autowired
	private IngredientRepository ingredientRepository;

	@Autowired
	private MealPlanItemRepository mealPlanItemRepository;

	@Override
	public List<Recipe> getAllRecipes() {
		// TODO Auto-generated method stub
		return recipeRepository.findAll();
	}

	@Override
	public Recipe getById(Integer id) {
		// TODO Auto-generated method stub
		return recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
	}

	@Override
	@Transactional
	public void save(Recipe recipe, List<Integer> ingredientIds, List<BigDecimal> quantities, List<String> units) {
		if (recipe.getName() != null) {
			recipe.setName(recipe.getName().trim());
		}
		if (recipe.getRecipeId() == null) {
			List<Recipe> existingList = recipeRepository.findByNameIgnoreCase(recipe.getName());
			if (!existingList.isEmpty()) {
				throw new IllegalArgumentException("Recipe name already exists!");
			}
			recipe.setIsActive(true);
			recipe.setUpdatedAt(LocalDateTime.now());
		} else {
			List<Recipe> duplicateCheck = recipeRepository.findByNameIgnoreCaseAndRecipeIdNot(recipe.getName(),
					recipe.getRecipeId());
			if (!duplicateCheck.isEmpty()) {
				throw new IllegalArgumentException("Recipe name already exists!");
			}
			Recipe existing = recipeRepository.findById(recipe.getRecipeId())
					.orElseThrow(() -> new RuntimeException("Recipe not found"));
			recipe.setIsActive(existing.getIsActive());
			recipe.setUpdatedAt(existing.getUpdatedAt());
			// not to disappear old img (maintain Image URL )
			if (recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty()) {
				recipe.setImageUrl(existing.getImageUrl());
			}
		}
		recipe.setUpdatedAt(LocalDateTime.now());
		recipeRepository.save(recipe);

		if (recipe.getRecipeId() != null) {
			recipeIngredientRepository.deleteByRecipe_RecipeId(recipe.getRecipeId());
		}

		if (ingredientIds != null && !ingredientIds.isEmpty()) {
			for (int i = 0; i < ingredientIds.size(); i++) {
				Integer ingId = ingredientIds.get(i);
				if (ingId != null) {
					Ingredient ingredient = ingredientRepository.findById(ingId).orElse(null);

					if (ingredient != null) {
						BigDecimal qty = (quantities != null && i < quantities.size() && quantities.get(i) != null)
								? quantities.get(i)
								: BigDecimal.ONE;

						IngredientUnit ingredientUnit = (units != null && i < units.size() && units.get(i) != null)
								? IngredientUnit.fromValue(units.get(i))
								: IngredientUnit.GRAM;

						RecipeIngredient ri = new RecipeIngredient(recipe, ingredient, qty, ingredientUnit);
						recipeIngredientRepository.save(ri);
					}
				}
			}
		}
	}

	@Override
	@Transactional
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		boolean isUsedInMealPlan = mealPlanItemRepository.existsByRecipe_RecipeId(id);
		if (isUsedInMealPlan) {
			throw new IllegalStateException(
					"Cannot delete this recipe because it is currently being used in users' meal plans.");
		}

		Recipe recipe = recipeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
		recipe.setIsActive(false);
		recipe.setUpdatedAt(LocalDateTime.now());

		recipeRepository.save(recipe);
	}

	// Status (Active/Inactive)
	@Transactional
	public void toggleStatus(Integer id) {
		Recipe recipe = getById(id);
		recipe.setIsActive(!recipe.getIsActive());
		recipeRepository.save(recipe);
	}

	@Override
	public void save(Recipe recipe) {
		save(recipe, null, null, null);
	}

	@Override
	public void save(Recipe recipe, List<Integer> ingredientIds) {
		save(recipe, ingredientIds, null, null);
	}

	@Override
	@Transactional
	public void activate(Integer id) {
		Recipe recipe = recipeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
		recipe.setIsActive(true);
		recipe.setUpdatedAt(LocalDateTime.now());
		recipeRepository.save(recipe);
	}

	@Override
	public void loadEditFormAttributes(Integer recipeId, Model model) {
		List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByRecipe_RecipeId(recipeId);

		List<Integer> selectedIngredientIds = recipeIngredients.stream().map(ri -> ri.getIngredient().getIngredientId())
				.toList();
		model.addAttribute("selectedIngredientIds", selectedIngredientIds);

		Map<Integer, BigDecimal> ingredientQtys = recipeIngredients.stream()
				.collect(java.util.stream.Collectors.toMap(ri -> ri.getIngredient().getIngredientId(),
						RecipeIngredient::getQty, (existing, replacement) -> existing));
		model.addAttribute("ingredientQtys", ingredientQtys);

		Map<Integer, String> ingredientUnits = recipeIngredients.stream()
				.collect(java.util.stream.Collectors.toMap(ri -> ri.getIngredient().getIngredientId(),
						ri -> ri.getIngredientUnit() != null ? ri.getIngredientUnit().name() : "",
						(existing, replacement) -> existing));
		model.addAttribute("ingredientUnits", ingredientUnits);
	}

	@Override
	public List<Recipe> getActiveRecipes() {
		// TODO Auto-generated method stub
		return recipeRepository.findByIsActiveTrue();
	}

	// img handling helper
	public String saveImageFile(MultipartFile imageFile) throws IOException {
		if (imageFile == null || imageFile.isEmpty()) {
			return null;
		}
		String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
		File uploadDirFile = new File(uploadDir);
		if (!uploadDirFile.exists()) {
			uploadDirFile.mkdirs();
		}

		// add current time to avoid same filename
		String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
		Path filePath = Paths.get(uploadDir + fileName);
		Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		return fileName;
	}

}
