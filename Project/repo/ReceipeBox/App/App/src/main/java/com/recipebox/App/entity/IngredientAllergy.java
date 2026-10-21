package com.recipebox.App.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ingredient_allergens") // Junction Table
public class IngredientAllergy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "ingredient_id")
	private Ingredient ingredient;

	@ManyToOne
	@JoinColumn(name = "allergen_id")
	private Allergy allergy;

	// constructor
	public IngredientAllergy(Integer id, Ingredient ingredient, Allergy allergy) {
		super();
		this.id = id;
		this.ingredient = ingredient;
		this.allergy = allergy;
	}

	public IngredientAllergy() {
		super();
		// TODO Auto-generated constructor stub
	}

//////get set//////
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public Allergy getAllergy() {
		return allergy;
	}

	public void setAllergy(Allergy allergy) {
		this.allergy = allergy;
	}

}