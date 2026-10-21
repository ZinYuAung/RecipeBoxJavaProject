package com.recipebox.App.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recipebox.App.entity.Allergy;
import com.recipebox.App.entity.IngredientAllergy;
import com.recipebox.App.repository.IngredientAllergyRepository;
import com.recipebox.App.service.AllergyService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/allergy")
public class AllergyController {

	@Autowired
	private AllergyService allergyService;

	@Autowired
	private IngredientAllergyRepository ingredientAllergyRepository;

	@GetMapping({ "", "/list" })
	public String listAllergens(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) Boolean status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Model model) {

		List<Allergy> allergens = allergyService.searchAllergies(keyword);

		if (status != null) {
			boolean isDeletedVal = !status; //if Active (true) , isDeleted (false) 
			allergens = allergens.stream().filter(a -> (a.getIsDeleted() != null && a.getIsDeleted() == isDeletedVal)
					|| (a.getIsDeleted() == null && !isDeletedVal)).toList();
		}
		// Manual Pagination for Filtered List
		Pageable pageable = PageRequest.of(page, size);
		int startItem = (int) pageable.getOffset();
		List<Allergy> paginatedList;

		if (allergens.size() < startItem) {
			paginatedList = List.of();
		} else {
			int toIndex = Math.min(startItem + pageable.getPageSize(), allergens.size());
			paginatedList = allergens.subList(startItem, toIndex);
		}

		Page<Allergy> allergyPage = new PageImpl<>(paginatedList, pageable, allergens.size());

		model.addAttribute("allergens", allergyPage.getContent());
		model.addAttribute("allergyPage", allergyPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedStatus", status);
		return "allergens/list";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("allergen", new Allergy());
		return "allergens/create";
	}

	@PostMapping("/save")
	public String saveAllergen(@Valid @ModelAttribute("allergen") Allergy allergen, BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			return "allergens/create";
		}
		try {
			allergyService.save(allergen);
		} catch (IllegalArgumentException e) {
			// same name error
			bindingResult.rejectValue("name", "error.allergen", e.getMessage());
			return "allergens/create";
		}
		return "redirect:/admin/allergy/list";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable int id, Model model) {
		model.addAttribute("allergen", allergyService.getById(id));
		return "allergens/edit";
	}

	@PostMapping("/update")
	public String updateAllergen(@Valid @ModelAttribute("allergen") Allergy allergen, BindingResult bindingResult,
			Model model, RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			return "allergens/edit";
		}

		try {
			allergyService.save(allergen);
		} catch (IllegalArgumentException e) {
			bindingResult.rejectValue("name", "error.allergen", e.getMessage());
			return "allergens/edit";
		}
		redirectAttributes.addFlashAttribute("message", "Allergy updated successfully!");
		return "redirect:/admin/allergy/list";
	}

	@PostMapping("/delete")
	public String deleteAllergen(@RequestParam int id, RedirectAttributes redirectAttributes) {
		try {
			allergyService.delete(id);
			redirectAttributes.addFlashAttribute("message", "Allergy deleted successfully.");
		} catch (IllegalStateException e) {
			// if use in active meal plan , error
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete allergy!");
		}
		return "redirect:/admin/allergy/list";
	}

	@PostMapping("/activate")
	public String activateAllergen(@RequestParam int id, RedirectAttributes redirectAttributes) {
		try {
			allergyService.activate(id);
			redirectAttributes.addFlashAttribute("message", "Allergy activated successfully.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate allergy: " + e.getMessage());
		}
		return "redirect:/admin/allergy/list";
	}

	@GetMapping("/view/{id}")
	public String viewAllergen(@PathVariable int id, Model model) {
		model.addAttribute("allergen", allergyService.getById(id));

		List<IngredientAllergy> relatedIngredients = ingredientAllergyRepository.findByAllergy_AllergenId(id);
		model.addAttribute("relatedIngredients", relatedIngredients);
		return "allergens/view";
	}
}