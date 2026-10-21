package com.recipebox.App.entity;

import java.math.BigDecimal;

import com.recipebox.App.enums.IngredientUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	@JoinColumn(name = "recipe_id")
	private Recipe recipe;

	@ManyToOne
	@JoinColumn(name = "ingredient_id")
	private Ingredient ingredient;

	
	private BigDecimal qty;

	@Enumerated(EnumType.STRING)
	@Column(name = "unit")
	private IngredientUnit ingredientUnit;

	public RecipeIngredient(int id, Recipe recipe, Ingredient ingredient, BigDecimal qty,
			IngredientUnit ingredientUnit) {
		super();
		this.id = id;
		this.recipe = recipe;
		this.ingredient = ingredient;
		this.qty = qty;
		this.ingredientUnit = ingredientUnit;
	}

	public RecipeIngredient(Recipe recipe, Ingredient ingredient, BigDecimal qty, IngredientUnit ingredientUnit) {
		this.recipe = recipe;
		this.ingredient = ingredient;
		this.qty = qty;
		this.ingredientUnit = ingredientUnit;
	}

	public RecipeIngredient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public void setIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public IngredientUnit getIngredientUnit() {
		return ingredientUnit;
	}

	public void setIngredientUnit(IngredientUnit ingredientUnit) {
		this.ingredientUnit = ingredientUnit;
	}

}