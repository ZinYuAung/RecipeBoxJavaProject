package com.recipebox.App.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recipebox.App.entity.User;
import com.recipebox.App.service.FavoriteService;
import com.recipebox.App.service.RecipeService;

@Controller
public class FavoriteController {

	@Autowired
	private FavoriteService favoriteService;

	@Autowired
	private RecipeService recipeService;

	@GetMapping("/user/favorite-recipes")
	public String showFavoriteRecipes(Model model, HttpSession session) {
		User loginUser = (User) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/login";
		}
		model.addAttribute("favoriteRecipes", favoriteService.getFavoriteRecipesByUser(loginUser.getUserId()));
		List<Integer> userFavoriteIds = favoriteService.getFavoriteRecipeIdsByUser(loginUser.getUserId());
		model.addAttribute("userFavoriteIds", userFavoriteIds);
		return "dashboard/favorite-recipes";
	}

	@PostMapping("/user/favorites/toggle/{id}")
	public String toggleFavoriteRecipe(@PathVariable("id") int recipeId, 
	                                   HttpSession session,
	                                   HttpServletRequest request, 
	                                   RedirectAttributes redirectAttributes) {
	    User loginUser = (User) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return "redirect:/login";
	    }

	    try {
	        boolean isAdded = favoriteService.toggleFavorite(loginUser.getUserId(), recipeId);
	        if (isAdded) {
	            redirectAttributes.addFlashAttribute("message", "Recipe added to favorites!");
	        } else {
	            redirectAttributes.addFlashAttribute("message", "Recipe removed from favorites.");
	        }
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("error", e.getMessage());
	    }

	    // 💡 Get the page URL the request came from
	    String referer = request.getHeader("Referer");

	    // Redirect back to the previous page if available, otherwise default to dashboard
	    return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/user/dashboard";
	}

	/*
	 * @GetMapping("/user/recipe-view/{id}") public String
	 * viewRecipe(@PathVariable("id") int id, Model model, HttpSession session) {
	 * User loginUser = (User) session.getAttribute("loginUser"); if (loginUser ==
	 * null) { return "redirect:/login"; }
	 * 
	 * model.addAttribute("recipe", recipeService.getById(id));
	 * model.addAttribute("isAdmin", false); return "recipes/view"; }
	 */
}