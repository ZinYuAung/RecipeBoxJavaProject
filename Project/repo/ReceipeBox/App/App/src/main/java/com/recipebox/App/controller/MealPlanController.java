package com.recipebox.App.controller;

import com.recipebox.App.entity.*;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.repository.*;
import com.recipebox.App.service.MealPlanService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class MealPlanController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MealPlanRepository mealPlanRepository;

	@Autowired
	private MealPlanService mealPlanService;

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private AllergyRepository allergyRepository;

	@GetMapping("/meal-plans/suggest")
	public String generateSuggestedPlan(@RequestParam(required = false) String category, 
	                                   HttpSession session, 
	                                   Model model) {
	    User sessionUser = (User) session.getAttribute("loginUser");
	    if (sessionUser == null) {
	        return "redirect:/login";
	    }

	    // Refresh user from repository to get updated allergies
	    User currentUser = userRepository.findById(sessionUser.getUserId()).orElse(sessionUser);

	    String bmiCategoryString = (category != null) ? category
	            : (String) session.getAttribute("calculatedBmiCategory");
	    if (bmiCategoryString == null) {
	        bmiCategoryString = "NORMAL";
	    }
	    BMICategory bmiCat = BMICategory.fromString(bmiCategoryString); 

	    List<Integer> userAllergenIds = allergyRepository.findSubscribedAllergenIdsByUserId(currentUser.getUserId());
	    List<MealPlanItem> temporaryItems = mealPlanService.generate7DayMealPlan(bmiCategoryString, userAllergenIds);
	   
	    session.setAttribute("transientSuggestedItems", temporaryItems);
	    session.setAttribute("calculatedBmiCategory", bmiCategoryString);
	    model.addAttribute("bmiCategory", bmiCategoryString);
	    model.addAttribute("suggestedItems", temporaryItems);
	    model.addAttribute("isSavedView", false);
	    model.addAttribute("minCal", bmiCat.getMinMealCalories());
	    model.addAttribute("maxCal", bmiCat.getMaxMealCalories());

	    return "bmi/suggest-plan";
	}

	@PostMapping("/meal-plans/save")
	@Transactional
	public String commitMealPlan(@RequestParam("bmiCategory") String bmiCategory, HttpSession session,
			RedirectAttributes redirectAttributes) {

		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null)
			return "redirect:/login";
		@SuppressWarnings("unchecked")
		List<MealPlanItem> transientItems = (List<MealPlanItem>) session.getAttribute("transientSuggestedItems");
		if (transientItems == null || transientItems.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "No active plan session found to commit.");
			return "redirect:/user/dashboard";
		}
		User managedUser = userRepository.findById(sessionUser.getUserId())
				.orElseThrow(() -> new IllegalStateException("Active authentication identity missing in database."));

		// Reset all previous active plans for this user to false
		mealPlanRepository.deactivateAllPlansForUser(managedUser.getUserId());
		MealPlan persistentPlan = new MealPlan();
		persistentPlan.setUser(managedUser);
		persistentPlan.setBmiCategory(BMICategory.valueOf(bmiCategory.toUpperCase().trim()));
		persistentPlan.setCreatedAt(LocalDateTime.now());
		persistentPlan.setIsActive(true);

		List<MealPlanItem> persistentItems = new ArrayList<>();
		for (MealPlanItem transientItem : transientItems) {
			MealPlanItem dbItem = new MealPlanItem();
			dbItem.setMealPlan(persistentPlan);
			dbItem.setDayNumber(transientItem.getDayNumber());
			dbItem.setMealType(transientItem.getMealType());
			dbItem.setRecipe(transientItem.getRecipe());
			persistentItems.add(dbItem);
		}
		persistentPlan.setItems(persistentItems);
		mealPlanRepository.save(persistentPlan);
		session.removeAttribute("transientSuggestedItems");
		redirectAttributes.addFlashAttribute("message", "Success! Plan saved and activated as your current meal plan.");
		return "redirect:/user/saved-plans";
	}

	@GetMapping("/saved-plans")
	public String viewSavedPlansList(HttpSession session, Model model) {
		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null)
			return "redirect:/login";
		List<MealPlan> plans = mealPlanRepository
				.findByUserUserIdOrderByIsActiveDescCreatedAtDesc(sessionUser.getUserId());

		model.addAttribute("savedPlans", plans);
		return "bmi/saved-plans";
	}

	@PostMapping("/meal-plans/set-active/{planId}")
	@Transactional
	public String setActivePlan(@PathVariable("planId") Integer planId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null)
			return "redirect:/login";
		mealPlanRepository.deactivateAllPlansForUser(sessionUser.getUserId());
		MealPlan targetPlan = mealPlanRepository.findById(planId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));
		if (targetPlan.getUser().getUserId() == sessionUser.getUserId()) {
			targetPlan.setIsActive(true);
			mealPlanRepository.save(targetPlan);
			redirectAttributes.addFlashAttribute("message",
					"Diet Plan is now set as your active plan!");
		}
		return "redirect:/user/saved-plans";
	}

	@GetMapping("/meal-plans/view/{id}")
	public String viewSavedMealPlan(@PathVariable("id") Integer planId, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null)
			return "redirect:/login";
		MealPlan savedPlan = mealPlanService.getMealPlanForUser(planId, sessionUser.getUserId()).orElse(null);

		if (savedPlan == null) {
			redirectAttributes.addFlashAttribute("error", "Meal plan not found or access denied.");
			return "redirect:/user/saved-plans";
		}
		model.addAttribute("savedPlan", savedPlan);
		model.addAttribute("suggestedItems", savedPlan.getItems());
		model.addAttribute("bmiCategory", savedPlan.getBmiCategory().name());
		model.addAttribute("isSavedView", true);
		return "bmi/suggest-plan";
	}

	@GetMapping("/meal-plans/view/{planId}/day/{dayNum}")
	public String viewDayPlanDetails(@PathVariable("planId") Integer planId, @PathVariable("dayNum") Integer dayNum,
			HttpSession session, Model model, RedirectAttributes redirectAttributes) {

		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null)
			return "redirect:/login";

		MealPlan savedPlan = mealPlanService.getMealPlanForUser(planId, sessionUser.getUserId()).orElse(null);

		if (savedPlan == null) {
			redirectAttributes.addFlashAttribute("error", "Meal plan not found or access denied.");
			return "redirect:/user/saved-plans";
		}

		List<MealPlanItem> dayItems = mealPlanService.getItemsForDay(planId, sessionUser.getUserId(), dayNum);

		model.addAttribute("savedPlan", savedPlan);
		model.addAttribute("day", dayNum);
		model.addAttribute("dayItems", dayItems);

		return "bmi/day-plan-details";
	}

	@GetMapping("/recipe-details/{id}")
	public String viewRecipe(@PathVariable int id, Model model) {
		model.addAttribute("recipe", recipeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id:" + id)));
		return "recipes/view";
	}
	
	@PostMapping("/meal-plans/delete")
	public String deleteMealPlan(@RequestParam("planId") int planId,
	                             HttpSession session,
	                             RedirectAttributes redirectAttributes) {
	    User loginUser = (User) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return "redirect:/login";
	    }

	    try {
	        mealPlanService.deleteMealPlan(planId, loginUser.getUserId());
	        redirectAttributes.addFlashAttribute("message", "Meal plan deleted successfully!");
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("error", e.getMessage());
	    }

	    return "redirect:/user/saved-plans";
	}
}