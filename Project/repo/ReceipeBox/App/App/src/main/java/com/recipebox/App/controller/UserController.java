package com.recipebox.App.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.recipebox.App.entity.Recipe;
import com.recipebox.App.entity.User;
import com.recipebox.App.entity.UserAllergy;
import com.recipebox.App.enums.MealType; // <--- MAKE SURE THIS IMPORT IS PRESENT
import com.recipebox.App.repository.RecipeRepository;
import com.recipebox.App.repository.UserAllergyRepository;
import com.recipebox.App.repository.UserRepository;
import com.recipebox.App.service.FavoriteService;
import com.recipebox.App.service.RecipeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserAllergyRepository userAllergyRepository;

	@Autowired
	private FavoriteService favoriteService;

	@GetMapping("/dashboard")
	public String dashboard(@RequestParam(value = "mealType", required = false) String mealType, HttpSession session,
			Model model) {
		User currentUser = (User) session.getAttribute("loginUser");
		if (currentUser == null) {
			return "redirect:/login";
		}
		model.addAttribute("user", currentUser);

		List<Recipe> recipes;

		if (mealType != null && !mealType.trim().isEmpty()) {
			try {
				MealType type = MealType.valueOf(mealType.toUpperCase());
				recipes = recipeRepository.findByMealTypeAndIsActiveTrue(type);
				model.addAttribute("selectedMealType", mealType);
			} catch (IllegalArgumentException e) {
				recipes = recipeRepository.findByIsActiveTrue();
			}
		} else {
			recipes = recipeRepository.findByIsActiveTrue();
		}

		model.addAttribute("recipes", recipes);
		List<Integer> userFavoriteIds = favoriteService.getFavoriteRecipeIdsByUser(currentUser.getUserId());
		model.addAttribute("userFavoriteIds", userFavoriteIds);
		return "dashboard/user-dashboard";
	}

	@GetMapping("/profile")
	public String viewProfile(HttpSession session, Model model) {
		User currentUser = (User) session.getAttribute("loginUser");
		if (currentUser == null) {
			return "redirect:/login";
		}
		List<UserAllergy> userAllergies = userAllergyRepository.findByUser(currentUser);
		model.addAttribute("user", currentUser);
		model.addAttribute("userAllergies", userAllergies);
		model.addAttribute("isSelfView", true);

		return "user/view";
	}

	@GetMapping("/profile/edit")
	public String showProfileEditForm(HttpSession session, Model model) {
		User currentUser = (User) session.getAttribute("loginUser");
		if (currentUser == null) {
			return "redirect:/login";
		}

		model.addAttribute("user", currentUser);
		model.addAttribute("isSelfEdit", true);

		return "user/edit";
	}

	@PostMapping("/profile/update")
	public String updateProfile(@ModelAttribute User updatedUser,
			@RequestParam(value = "currentPassword", required = false) String currentPassword,
			@RequestParam(value = "newPassword", required = false) String newPassword,
			@RequestParam(value = "confirmPassword", required = false) String confirmPassword, HttpSession session,
			Model model) {

		User currentUser = (User) session.getAttribute("loginUser");
		if (currentUser == null) {
			return "redirect:/login";
		}

		User databaseUser = userRepository.findById(currentUser.getUserId()).orElseThrow();

		if (updatedUser.getEmail() != null
				&& !updatedUser.getEmail().trim().equalsIgnoreCase(databaseUser.getEmail())) {
			java.util.Optional<User> emailCheck = userRepository.findByEmail(updatedUser.getEmail().trim());
			if (emailCheck.isPresent() && emailCheck.get().getUserId() != databaseUser.getUserId()) {
				model.addAttribute("user", databaseUser);
				model.addAttribute("errorMessage", "This email is already in use by another account.");
				return "user/edit"; 
			}
		}
		// Always update name and email regardless of password change
		databaseUser.setName(updatedUser.getName());
		databaseUser.setEmail(updatedUser.getEmail());

		// ONLY process password changes if the user typed something into newPassword
		if (newPassword != null && !newPassword.trim().isEmpty()) {

			if (currentPassword == null || currentPassword.trim().isEmpty()) {
				return "redirect:/user/profile/edit?error=currentPasswordRequired";
			}

			if (!passwordEncoder.matches(currentPassword, databaseUser.getPassword_hash())) {
				return "redirect:/user/profile/edit?error=wrongCurrentPassword";
			}

			if (!newPassword.equals(confirmPassword)) {
				return "redirect:/user/profile/edit?error=passwordMismatch";
			}

			databaseUser.setPassword_hash(passwordEncoder.encode(newPassword));
		}
		try {
			userRepository.save(databaseUser);
			session.setAttribute("loginUser", databaseUser);
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			model.addAttribute("user", databaseUser);
			model.addAttribute("errorMessage",
					"This email address is already registered. Please use a different email.");
			return "user/edit";
		}

		return "redirect:/user/dashboard?success=profileUpdated";
	}

	@GetMapping("/recipes/search")
	public String searchRecipes(@RequestParam(value = "query", required = false) String query, HttpSession session,
			Model model) {

		User currentUser = (User) session.getAttribute("loginUser");
		if (currentUser == null) {
			return "redirect:/login";
		}

		if (query == null || query.trim().isEmpty()) {
			return "redirect:/user/dashboard";
		}

		List<Recipe> searchResults = recipeRepository.searchByKeywordInRecipeOrIngredient(query.trim());

		model.addAttribute("recipes", searchResults);
		model.addAttribute("searchQuery", query);

		List<Integer> userFavoriteIds = favoriteService.getFavoriteRecipeIdsByUser(currentUser.getUserId());
		model.addAttribute("userFavoriteIds", userFavoriteIds);
		return "dashboard/user-dashboard";
	}
}