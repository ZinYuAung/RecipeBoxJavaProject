package com.recipebox.App.service;

import java.util.List;
import com.recipebox.App.entity.Allergy;

public interface AllergyService {
	List<Allergy> getAllAllergy();
	List<Allergy> getActiveAllergies();
	Allergy getById(int id);
	void save(Allergy allergy);
	void delete(int id);
	void activate(int id);
	List<Allergy> searchAllergies(String keyword);

}
