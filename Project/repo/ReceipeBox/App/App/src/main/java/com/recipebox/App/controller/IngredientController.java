package com.recipebox.App.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recipebox.App.entity.Ingredient;
import com.recipebox.App.repository.AllergyRepository;
import com.recipebox.App.repository.IngredientAllergyRepository;
import com.recipebox.App.service.IngredientService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/ingredient")
public class IngredientController {

	@Autowired
	private IngredientService ingredientService;

	@Autowired
	private AllergyRepository allergyRepository;

	@Autowired
	private IngredientAllergyRepository ingredientAllergyRepository;
	@GetMapping({ "", "/list" })
	public String getIngredientList(@RequestParam(value = "keyword", required = false) String keyword,
	        @RequestParam(value = "allergy", required = false) List<String> allergies,
	        @RequestParam(value = "status", required = false) Boolean status,
	        @RequestParam(value = "page", defaultValue = "0") int page,
	        @RequestParam(value = "size", defaultValue = "10") int size, Model model) {

	    // If a user passes a comma-joined single string parameter accidentally, split it safely
	    List<String> processedAllergies = new ArrayList<>();
	    if (allergies != null) {
	        for (String a : allergies) {
	            if (a != null && !a.trim().isEmpty()) {
	                for (String splitVal : a.split(",")) {
	                    if (!splitVal.trim().isEmpty()) {
	                        processedAllergies.add(splitVal.trim().toLowerCase());
	                    }
	                }
	            }
	        }
	    }

	    List<Ingredient> ingredients = (keyword != null && !keyword.trim().isEmpty())
	            ? ingredientService.getAllIngredients(keyword)
	            : ingredientService.getAllIngredients();

	    // Filter by Multiple Allergies (OR condition)
	    if (!processedAllergies.isEmpty()) {
	        List<Integer> matchingIngredientIds = ingredientAllergyRepository
	                .findByAllergyNameIgnoreCaseIn(processedAllergies)
	                .stream()
	                .map(ia -> ia.getIngredient().getIngredientId())
	                .distinct()
	                .toList();

	        ingredients = ingredients.stream()
	                .filter(ing -> matchingIngredientIds.contains(ing.getIngredientId()))
	                .toList();
	    }

	    // Status Filter logic...
	    if (status != null) {
	        boolean isDeletedVal = !status; 
	        ingredients = ingredients.stream()
	                .filter(ing -> (ing.getIsDeleted() != null && ing.getIsDeleted() == isDeletedVal)
	                        || (ing.getIsDeleted() == null && !isDeletedVal))
	                .toList();
	    }

	    // Pagination logic...
	    Pageable pageable = PageRequest.of(page, size);
	    int startItem = (int) pageable.getOffset();
	    List<Ingredient> paginatedList;

	    if (ingredients.size() < startItem) {
	        paginatedList = List.of();
	    } else {
	        int toIndex = Math.min(startItem + pageable.getPageSize(), ingredients.size());
	        paginatedList = ingredients.subList(startItem, toIndex);
	    }

	    Page<Ingredient> ingredientPage = new org.springframework.data.domain.PageImpl<>(paginatedList, pageable, ingredients.size());

	    model.addAttribute("ingredients", ingredientPage.getContent());
	    model.addAttribute("ingredientPage", ingredientPage);
	    model.addAttribute("keyword", keyword); 
	    model.addAttribute("selectedAllergy", processedAllergies); 
	    model.addAttribute("selectedStatus", status);
	    model.addAttribute("allAllergens", allergyRepository.findByIsDeletedFalseOrderByNameAsc());

	    return "ingredients/list";
	}
	@GetMapping("/add")
	public String createNewIngredient(Model model) {
		model.addAttribute("ingredient", new Ingredient());
		model.addAttribute("allAllergens", allergyRepository.findByIsDeletedFalseOrderByNameAsc());
		return "ingredients/add";
	}

	@PostMapping("/save")
	public String saveIngredient(@Valid @ModelAttribute("ingredient") Ingredient ingredient,
			BindingResult bindingResult,
			@RequestParam(value = "allergenIds", required = false) List<Integer> allergenIds, Model model,
			RedirectAttributes redirectAtt) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("allAllergens", allergyRepository.findByIsDeletedFalseOrderByNameAsc());
			model.addAttribute("allergenIds", allergenIds);
			return (ingredient.getIngredientId() == null) ? "ingredients/add" : "ingredients/edit";
		}
		try {
			ingredientService.save(ingredient, allergenIds);
		} catch (IllegalArgumentException e) {
			model.addAttribute("allAllergens", allergyRepository.findByIsDeletedFalseOrderByNameAsc());
			model.addAttribute("allergenIds", allergenIds);
			bindingResult.rejectValue("name", "error.ingredient", e.getMessage());
			return (ingredient.getIngredientId() == null) ? "ingredients/add" : "ingredients/edit";
		}
		redirectAtt.addFlashAttribute("message", "Successfully created new ingredient!");
		return "redirect:/admin/ingredient/list";
	}

	@GetMapping("/edit/{id}")
	public String editIngredient(@PathVariable Integer id, Model model) {
		model.addAttribute("ingredient", ingredientService.getById(id));
		model.addAttribute("allAllergens", allergyRepository.findByIsDeletedFalseOrderByNameAsc());
		return "ingredients/edit";
	}

	@GetMapping("/view/{id}")
	public String viewIngredientDetails(@PathVariable Integer id, Model model) {
		model.addAttribute("ingredient", ingredientService.getById(id));
		return "ingredients/view";
	}

	@PostMapping("/delete/{id}")
	public String deleteIngredient(@PathVariable Integer id, RedirectAttributes redirectAttributes)
			throws IllegalStateException {
		try {
			ingredientService.delete(id);
			redirectAttributes.addFlashAttribute("message", "Ingredient deleted successfully.");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete ingredient");
		}
		return "redirect:/admin/ingredient/list";
	}

	@PostMapping("/activate")
	public String activateIngredient(@RequestParam int id, RedirectAttributes redirectAttributes) {
		try {
			ingredientService.activate(id);
			redirectAttributes.addFlashAttribute("message", "Ingredient activated successfully.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate ingredient: ");
		}
		return "redirect:/admin/ingredient/list";
	}
}