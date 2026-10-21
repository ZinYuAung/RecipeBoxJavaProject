package com.recipebox.App.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.recipebox.App.entity.Allergy;
import com.recipebox.App.entity.BmiRecord;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.entity.User;
import com.recipebox.App.entity.UserAllergy;
import com.recipebox.App.repository.AllergyRepository;
import com.recipebox.App.repository.BmiRepository;
import com.recipebox.App.repository.UserAllergyRepository;
import com.recipebox.App.repository.UserRepository;
import com.recipebox.App.service.AllergyService;
import com.recipebox.App.service.BmiService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user/bmi")
public class BmiController {

	@Autowired
	private BmiRepository bmiRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BmiService bmiService;

	@Autowired
	private AllergyService allergyService;

	@Autowired
	private UserAllergyRepository userAllergyRepository;

	@Autowired
	private AllergyRepository allergyRepository;

	@GetMapping({ "", "/" })
	public String viewBmiDashboard(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null) {
			return "redirect:/login";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<BmiRecord> bmiPage = bmiService.getUserBmiHistoryPage(sessionUser.getUserId(), pageable);
		BmiRecord latestRecord = bmiService.getLatestBmiRecord(sessionUser.getUserId()).orElse(null);
		model.addAttribute("latestRecord", latestRecord);
		model.addAttribute("bmiPage", bmiPage);
		model.addAttribute("bmiHistory", bmiPage.getContent());
		model.addAttribute("page", "bmi");

		return "bmi/bmi-history";
	}

	
	
	@GetMapping("/bmiForm")
	public String bmiForm(HttpSession session, Model model) {
		User currentUser = (User) session.getAttribute("loginUser");
		
		// no login user
		if (currentUser != null) {
			model.addAttribute("userId", currentUser.getUserId());
			List<Allergy> activeAllergens = allergyService.getAllAllergy().stream()
					.filter(a -> a.getIsDeleted() == null || !a.getIsDeleted()).toList();
			model.addAttribute("allAllergens", activeAllergens);
			
			List<UserAllergy> userAllergies = userAllergyRepository.findByUser(currentUser);
			List<Integer> existingAllergenIds = userAllergies.stream().map(ua -> ua.getAllergen().getAllergenId()).toList();
			model.addAttribute("userAllergenIds", existingAllergenIds);
		} else {
			model.addAttribute("userId", 0);
		}
		
		model.addAttribute("bmiForm", new BmiRecord());
		return "bmi/bmiForm";
	}

	@PostMapping("/calculate")
	public String calculateBmi(@Valid @ModelAttribute("bmiForm") BmiRecord bmiRecord, BindingResult bindingResult,
			@RequestParam(required = false) List<Integer> allergenIds,
			@RequestParam(defaultValue = "metric") String unit, @RequestParam(required = false) Double feet, 																									
			@RequestParam(required = false) Double heightInches, @RequestParam int userId, Model model,
			HttpSession session) {
		User currentUser = (User) session.getAttribute("loginUser");
		/*
		 * if (currentUser == null) { return "redirect:/login"; }
		 */
		if (feet != null)
			bmiRecord.setFeet(feet);
		if (heightInches != null)
			bmiRecord.setHeightInches(heightInches);
		boolean actualHasErrors = false;
		for (FieldError error : bindingResult.getFieldErrors()) {
			String field = error.getField();
			if ("metric".equalsIgnoreCase(unit) && (field.equals("feet") || field.equals("heightInches"))) {
				continue;
			}
			if ("imperial".equalsIgnoreCase(unit) && field.equals("height")) {
				continue;
			}
			actualHasErrors = true;
			break;
		}
		if ("imperial".equalsIgnoreCase(unit) && (bmiRecord.getFeet() == null || bmiRecord.getFeet() <= 0)) {
			actualHasErrors = true;
		}
		if (actualHasErrors) {
			model.addAttribute("userId", currentUser != null ? currentUser.getUserId() : userId);
			model.addAttribute("allergenIds", allergenIds);
			// model.addAttribute("allAllergens", allergyService.getAllAllergy());
			model.addAttribute("currentUnit", unit);

			model.addAttribute("feetValue", bmiRecord.getFeet());
			model.addAttribute("inchesValue", bmiRecord.getHeightInches());

			List<Allergy> activeAllergens = allergyService.getAllAllergy().stream()
					.filter(a -> a.getIsDeleted() == null || !a.getIsDeleted()).toList();
			model.addAttribute("allAllergens", activeAllergens);
			model.addAttribute("userAllergenIds", allergenIds);
			return "bmi/bmiForm";
		}
		BmiRecord processedRecord = bmiService.calculateBmiValues(bmiRecord, unit, bmiRecord.getHeightInches(),
				bmiRecord.getFeet());

		model.addAttribute("bmiResult", String.format("%.2f", processedRecord.getBmiValue()));
		model.addAttribute("category", processedRecord.getBmiCategory().name());
		String cssClass = switch (processedRecord.getBmiCategory()) {
		case UNDERWEIGHT -> "color-blue";
		case NORMAL -> "color-green";
		case OVERWEIGHT -> "color-yellow";
		case OBESE -> "color-red";
		};
		model.addAttribute("cssClass", cssClass);
		model.addAttribute("weight",  processedRecord.getWeight());
		model.addAttribute("height", processedRecord.getHeight());
		model.addAttribute("age", processedRecord.getAge());
		model.addAttribute("gender", processedRecord.getGender());
		model.addAttribute("userId", currentUser != null ? currentUser.getUserId() : userId);
		model.addAttribute("allergenIds", allergenIds);
		return "bmi/bmiResult";
	}

	@PostMapping("/save")
	@Transactional
	public String saveBmi(@RequestParam double bmiValue, @RequestParam String category, @RequestParam int age,
			@RequestParam String gender, @RequestParam int userId, @RequestParam Double weight,
			@RequestParam Double height, @RequestParam(required = false) List<Integer> allergenIds,
			RedirectAttributes redirectAttributes,HttpSession session) {
		User currentUser = (User) session.getAttribute("loginUser");
		if (currentUser == null) {
			redirectAttributes.addFlashAttribute("error", "Please login first to save your BMI record.");
			return "redirect:/login";
		}
		User user = userRepository.findById(userId).orElseThrow();

		BmiRecord record = new BmiRecord();
		record.setBmiValue(bmiValue);
		record.setBmiCategory(BMICategory.fromString(category));
		record.setAge(age);
		record.setGender(gender);
		record.setHeight(height);
		record.setWeight(weight);
		record.setUser(user);
		record.setRecordedAt(LocalDateTime.now());
		bmiRepository.save(record);

		if (allergenIds != null && !allergenIds.isEmpty()) {
	        List<Integer> distinctAllergenIds = allergenIds.stream().distinct().toList();
	        userAllergyRepository.deleteByUserCustom(user);
	        List<UserAllergy> newAllergies = new ArrayList<>();
	        for (Integer allergenId : distinctAllergenIds) {
	            Allergy allergy = allergyRepository.findById(allergenId).orElse(null);
	            if (allergy != null) {
	                UserAllergy userAllergy = new UserAllergy();
	                userAllergy.setUser(user);
	                userAllergy.setAllergen(allergy);
	                newAllergies.add(userAllergy);
	            }
	        }
	        userAllergyRepository.saveAllAndFlush(newAllergies);
	    }

		redirectAttributes.addFlashAttribute("message", "BMI record saved successfully!");
		return "redirect:/user/bmi";
	}

	@PostMapping("/delete")
	public String deleteBmiRecord(@RequestParam("id") int recordId, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User sessionUser = (User) session.getAttribute("loginUser");
		if (sessionUser == null) {
			return "redirect:/login";
		}

		boolean deleted = bmiService.deleteBmiRecord(recordId, sessionUser.getUserId());

		if (deleted) {
			redirectAttributes.addFlashAttribute("message", "BMI record deleted successfully!");
		} else {
			redirectAttributes.addFlashAttribute("error", "Unable to delete record or record not found.");
		}

		return "redirect:/user/bmi";
	}
}