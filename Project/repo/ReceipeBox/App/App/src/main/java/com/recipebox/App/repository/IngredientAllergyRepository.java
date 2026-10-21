package com.recipebox.App.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.recipebox.App.entity.IngredientAllergy;

public interface IngredientAllergyRepository extends JpaRepository<IngredientAllergy, Integer> {
	
    void deleteByIngredient_IngredientId(Integer ingredientId);

    boolean existsByAllergyAllergenId(Integer allergenId);
    
    List<IngredientAllergy> findByAllergy_AllergenId(Integer allergenId);

    @Query("SELECT ia FROM IngredientAllergy ia LEFT JOIN FETCH ia.ingredient LEFT JOIN FETCH ia.allergy WHERE LOWER(TRIM(ia.allergy.name)) IN :allergyNames")
    List<IngredientAllergy> findByAllergyNameIgnoreCaseIn(@Param("allergyNames") List<String> allergyNames);
}