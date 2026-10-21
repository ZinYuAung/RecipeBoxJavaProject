package com.recipebox.App.controller;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recipebox.App.entity.Recipe;
import com.recipebox.App.entity.RecipeIngredient;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.enums.MealType;
import com.recipebox.App.repository.RecipeIngredientRepository;
import com.recipebox.App.service.IngredientService;
import com.recipebox.App.service.RecipeService;
import com.recipebox.App.serviceImpl.RecipeServiceImpl;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.WebDataBinder;

@Controller
@RequestMapping()
public class RecipeController {

	@Autowired
	private RecipeService recipeService;

	@Autowired
	private IngredientService ingredientService;

	@Autowired
	private RecipeIngredientRepository recipeIngredientRepository;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(MealType.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(MealType.fromValue(text));
			}
		});
	}

	@GetMapping({ "/admin/recipe", "/admin/recipe/list" })
	public String listRecipes(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "mealType", required = false) String mealType,
			@RequestParam(value = "bmiCategory", required = false) String bmiCategory,
			@RequestParam(value = "status", required = false) Boolean status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Model model) {

		List<Recipe> recipes = recipeService.getAllRecipes();

		// 1. Filter by keyword (Name)
		if (keyword != null && !keyword.trim().isEmpty()) {
			String lowerKeyword = keyword.toLowerCase().trim();
			recipes = recipes.stream()
					.filter(r -> r.getName() != null && r.getName().toLowerCase().contains(lowerKeyword)).toList();
		}

		// 2. Filter by Meal Type
		if (mealType != null && !mealType.trim().isEmpty()) {
			try {
				MealType mType = MealType.valueOf(mealType.toUpperCase().trim());
				recipes = recipes.stream().filter(r -> r.getMealType() != null && r.getMealType().equals(mType))
						.toList();
			} catch (IllegalArgumentException ignored) {
			}
		}

		// 3. Filter by BMI Category
		if (bmiCategory != null && !bmiCategory.trim().isEmpty()) {
			try {
				BMICategory bCat = BMICategory.valueOf(bmiCategory.toUpperCase().trim());
				recipes = recipes.stream().filter(r -> r.getBmiCategory() != null && r.getBmiCategory().equals(bCat))
						.toList();
			} catch (IllegalArgumentException ignored) {
			}
		}
		// 4. Filter by Status (Active/Inactive)
		if (status != null) {
			recipes = recipes.stream().filter(r -> r.getIsActive() == status).toList();
		}
		model.addAttribute("selectedStatus", status);

		// Manual Pagination for Filtered List
		Pageable pageable = PageRequest.of(page, size);
		int startItem = (int) pageable.getOffset();
		List<Recipe> paginatedList;

		if (recipes.size() < startItem) {
			paginatedList = List.of();
		} else {
			int toIndex = Math.min(startItem + pageable.getPageSize(), recipes.size());
			paginatedList = recipes.subList(startItem, toIndex);
		}

		Page<Recipe> recipePage = new PageImpl<>(paginatedList, pageable, recipes.size());

		model.addAttribute("recipes", recipePage.getContent());
		model.addAttribute("recipePage", recipePage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedMealType", mealType);
		model.addAttribute("selectedBmiCategory", bmiCategory);

		return "recipes/list";
	}

	@GetMapping("/admin/recipe/create")
	public String showCreateForm(Model model) {
		model.addAttribute("recipe", new Recipe());
		model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
		return "recipes/create";
	}

	@PostMapping("/admin/recipe/save")
	public String saveRecipe(@Valid @ModelAttribute("recipe") Recipe recipe, BindingResult bindingResult,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "ingredientIds", required = false) List<Integer> ingredientIds,
			@RequestParam(value = "quantities", required = false) List<BigDecimal> quantities,
			@RequestParam(value = "units", required = false) List<String> units, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			if (imageFile != null && !imageFile.isEmpty()) {
				String fileName = recipeService.saveImageFile(imageFile);
				recipe.setImageUrl(fileName);
			}
		} catch (Exception e) {

		}

		// check entity fields validation error
		if (bindingResult.hasErrors()) {
			model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
			// if error still show correct ingredient

			model.addAttribute("selectedIngredientIds", ingredientIds);
			model.addAttribute("quantities", quantities);
			model.addAttribute("units", units);
			return "recipes/create";
		}
		// check if choose at least one
		if (ingredientIds == null || ingredientIds.isEmpty()) {
			model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
			model.addAttribute("ingredientError", "Please select at least one ingredient.");
			model.addAttribute("selectedIngredientIds", ingredientIds);
			model.addAttribute("quantities", quantities);
			model.addAttribute("units", units);
			return "recipes/create";
		}
		try {
			/*
			 * // Image saving if (imageFile != null && !imageFile.isEmpty()) {
			 * //RecipeServiceImpl impl = (RecipeServiceImpl) recipeService; String fileName
			 * = recipeService.saveImageFile(imageFile); recipe.setImageUrl(fileName);
			 * 
			 * }
			 */
			recipeService.save(recipe, ingredientIds, quantities, units);
			redirectAttributes.addFlashAttribute("successMessage", "Recipe created successfully!");
		} catch (IllegalArgumentException e) {
			// if same name,Field Error
			model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
			model.addAttribute("selectedIngredientIds", ingredientIds);
			model.addAttribute("quantities", quantities);
			model.addAttribute("units", units);
			bindingResult.rejectValue("name", "error.recipe", e.getMessage());
			return "recipes/create";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to create recipe.Please try again later. ");
			return "redirect:/admin/recipe/create";
		}
		return "redirect:/admin/recipe/list";
	}

	@GetMapping("/admin/recipe/edit/{id}")
	public String showEditForm(@PathVariable int id, Model model) {
		Recipe recipe = recipeService.getById(id);
		model.addAttribute("recipe", recipe);
		model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
		recipeService.loadEditFormAttributes(id, model);
		// model.addAttribute("allIngredients", ingredientService.getAllIngredients());

		List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByRecipe_RecipeId(id);

		List<Integer> selectedIngredientIds = recipeIngredients.stream().map(ri -> ri.getIngredient().getIngredientId())
				.toList();
		model.addAttribute("selectedIngredientIds", selectedIngredientIds);

		Map<Integer, BigDecimal> ingredientQtys = recipeIngredients.stream()
				.collect(Collectors.toMap(ri -> ri.getIngredient().getIngredientId(), RecipeIngredient::getQty,
						(existing, replacement) -> existing));
		model.addAttribute("ingredientQtys", ingredientQtys);

		Map<Integer, String> ingredientUnits = recipeIngredients.stream()
				.collect(Collectors.toMap(ri -> ri.getIngredient().getIngredientId(),
						ri -> ri.getIngredientUnit() != null ? ri.getIngredientUnit().name() : "",
						(existing, replacement) -> existing));
		model.addAttribute("ingredientUnits", ingredientUnits);
		return "recipes/edit";
	}

	@PostMapping("/admin/recipe/update")
	public String updateRecipe(@Valid @ModelAttribute("recipe") Recipe recipe, BindingResult bindingResult,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "ingredientIds", required = false) List<Integer> ingredientIds,
			@RequestParam(value = "quantities", required = false) List<BigDecimal> quantities,
			@RequestParam(value = "units", required = false) List<String> units, Model model,
			RedirectAttributes redirectAttributes) {
		Recipe existingRecipe = recipeService.getById(recipe.getRecipeId());
		if (bindingResult.hasErrors()) {
			if (recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty()) {
				recipe.setImageUrl(existingRecipe.getImageUrl());
			}
			model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
			recipeService.loadEditFormAttributes(recipe.getRecipeId(), model);
			model.addAttribute("selectedIngredientIds", ingredientIds);
			model.addAttribute("quantities", quantities);
			model.addAttribute("units", units);
			return "recipes/edit";
		}
		if (ingredientIds == null || ingredientIds.isEmpty()) {
			if (recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty()) {
				recipe.setImageUrl(existingRecipe.getImageUrl());
			}
			model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
			model.addAttribute("ingredientError", "Please select at least one ingredient.");
			recipeService.loadEditFormAttributes(recipe.getRecipeId(), model);
			model.addAttribute("selectedIngredientIds", ingredientIds);
			model.addAttribute("quantities", quantities);
			model.addAttribute("units", units);
			return "recipes/edit";
		}
		try {
			// img update
			if (imageFile != null && !imageFile.isEmpty()) {
				// RecipeServiceImpl impl = (RecipeServiceImpl) recipeService;
				String fileName = recipeService.saveImageFile(imageFile);
				recipe.setImageUrl(fileName);
			} else {

				recipe.setImageUrl(existingRecipe.getImageUrl());
			}
			// if Update , adding quantities,units
			recipeService.save(recipe, ingredientIds, quantities, units);
			redirectAttributes.addFlashAttribute("message", "Recipe updated successfully!");
		} catch (IllegalArgumentException e) {
			//if same name
			if (recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty()) {
				recipe.setImageUrl(existingRecipe.getImageUrl());
			}
			model.addAttribute("allIngredients", ingredientService.getActiveIngredients());
			recipeService.loadEditFormAttributes(recipe.getRecipeId(), model);
			model.addAttribute("selectedIngredientIds", ingredientIds);
			model.addAttribute("quantities", quantities);
			model.addAttribute("units", units);
			bindingResult.rejectValue("name", "error.recipe", e.getMessage());
			return "recipes/edit";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to update recipe.Please try again later. ");
			return "redirect:/admin/recipe/edit/" + recipe.getRecipeId();

		}
		return "redirect:/admin/recipe/list";
	}

	@PostMapping("/admin/recipe/delete")
	public String deleteRecipe(@RequestParam int id, RedirectAttributes redirectAttributes) {
		try {
			recipeService.delete(id);
			redirectAttributes.addFlashAttribute("message", "Recipe deleted successfully.");
		} catch (IllegalStateException e) {
			// if use in active meal plan , error
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete recipe.Please try again later. ");
		}
		return "redirect:/admin/recipe/list";
	}

	@GetMapping("/admin/recipe/view/{id}")
	public String viewRecipe(@PathVariable int id, Model model, Authentication authentication) {
		model.addAttribute("recipe", recipeService.getById(id));
		boolean isAdmin = false;
		if (authentication != null && authentication.getAuthorities() != null) {
			if (authentication.getAuthorities()
					.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")) ||
					authentication.getAuthorities()
					.contains(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPERADMIN"))) {
				isAdmin = true;
			};
		}
		model.addAttribute("isAdmin", isAdmin);
		return "recipes/view";
	}

	@PostMapping("/admin/recipe/activate")
	public String activateRecipe(@RequestParam int id, RedirectAttributes redirectAttributes) {
		try {
			recipeService.activate(id);
			redirectAttributes.addFlashAttribute("successMessage", "Recipe activated successfully.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate recipe.Please try again later. ");
		}
		return "redirect:/admin/recipe/list";
	}

	@GetMapping({ "/user/recipe-view/{id}", "/view/{id}" })
	public String viewRecipe(@PathVariable("id") int id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl, HttpServletRequest request,
			HttpSession session, Model model, Authentication authentication) {

		model.addAttribute("recipe", recipeService.getById(id));
		boolean isAdmin = authentication != null && authentication.getAuthorities() != null
				&& authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
		model.addAttribute("isAdmin", isAdmin);
		String backUrl = determineBackUrl(returnUrl, request, session);
		model.addAttribute("backUrl", backUrl);
		return "recipes/view";
	}

	private String determineBackUrl(String returnUrl, HttpServletRequest request, HttpSession session) {
		if (returnUrl != null && !returnUrl.isBlank() && !returnUrl.contains("/recipe-view/")) {
			session.setAttribute("recipeBackUrl", returnUrl);
			return returnUrl;
		}
		String referer = request.getHeader("Referer");
		if (referer != null && !referer.isBlank() && !referer.contains("/recipe-view/")) {
			session.setAttribute("recipeBackUrl", referer);
			return referer;
		}
		String sessionBackUrl = (String) session.getAttribute("recipeBackUrl");
		if (sessionBackUrl != null && !sessionBackUrl.isBlank()) {
			return sessionBackUrl;
		}
		return "/user/dashboard";
	}

}