package com.recipebox.App.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipebox.App.entity.Allergy;
import com.recipebox.App.repository.AllergyRepository;
import com.recipebox.App.repository.IngredientAllergyRepository;
import com.recipebox.App.service.AllergyService;

@Service
public class AllergyServiceImpl implements AllergyService {

	@Autowired
	private AllergyRepository allergyRepository;

	@Autowired
	private IngredientAllergyRepository ingredientAllergyRepository;

	@Override
	public List<Allergy> getAllAllergy() {
		return allergyRepository.findAll();
	}
	
	@Override
	public List<Allergy> getActiveAllergies() {
		return allergyRepository.findByIsDeletedFalseOrderByNameAsc();
	}

	@Override
	public List<Allergy> searchAllergies(String keyword) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			return allergyRepository.findByNameContainingIgnoreCase(keyword);
		}
		return allergyRepository.findByIsDeletedFalseOrderByNameAsc();
	}

	@Override
	public Allergy getById(int id) {
		return allergyRepository.findById(id).orElseThrow(() -> new RuntimeException("Allergy not found"));
	}

	@Override
	@Transactional
	public void save(Allergy allergy) {
		if (allergy.getName() != null) {
			allergy.setName(allergy.getName().trim());
		}
		if (allergy.getAllergenId() == 0) {
			List<Allergy> existingList = allergyRepository.findByNameIgnoreCase(allergy.getName());
			if (!existingList.isEmpty()) {
				throw new IllegalArgumentException("Allergy name already exists!");
			}
			allergy.setCreatedAt(LocalDateTime.now());
			allergy.setIsDeleted(false);
		} else {
			List<Allergy> duplicateCheck = allergyRepository.findByNameIgnoreCaseAndAllergenIdNot(allergy.getName(), allergy.getAllergenId());
			if (!duplicateCheck.isEmpty()) {
				throw new IllegalArgumentException("Allergy name already exists!");
			}
			Allergy existing = allergyRepository.findById(allergy.getAllergenId())
					.orElseThrow(() -> new RuntimeException("Allergy not found"));
			allergy.setCreatedAt(existing.getCreatedAt());
			allergy.setIsDeleted(existing.getIsDeleted());
		}
		allergyRepository.save(allergy);
	}
	
	@Override
	@Transactional
	public void delete(int id) {
		Allergy allergy = allergyRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Allergy not found with id: " + id));
		
		boolean isUsed = ingredientAllergyRepository.existsByAllergyAllergenId(id);

		if (isUsed) {
			throw new IllegalStateException(
					"This allergy is currently associated with ingredients and cannot be deleted.");
		}
		//allergyRepository.deleteById(id);
		allergy.setIsDeleted(true);
		allergyRepository.save(allergy);
	}

	@Override
	@Transactional
	public void activate(int id) {
		Allergy allergy = allergyRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Allergy not found with id: " + id));
		
		allergy.setIsDeleted(false);
		allergyRepository.save(allergy);
	}


}
