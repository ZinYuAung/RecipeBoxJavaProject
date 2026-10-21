package com.recipebox.App.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ingredients")
public class Ingredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ingredient_id")
	private Integer ingredientId;

	@NotBlank(message = "Ingredient name cannot be blank.")
	@Size(max = 100, message = "Ingredient name must not exceed 100 characters.")
	@Pattern(regexp = "^$|.*[a-zA-Zа-яА-ЯğüşöçİĞÜŞÖÇ].*", message = "Ingredient name cannot be only numbers or invalid characters.")
	private String name;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

//	@OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<IngredientAllergy> ingredientAllergies = new ArrayList<>();
	
	@OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<IngredientAllergy> ingredientAllergies = new HashSet<>();

	public Set<IngredientAllergy> getIngredientAllergies() {
	    return ingredientAllergies;
	}

	public void setIngredientAllergies(Set<IngredientAllergy> ingredientAllergies) {
	    this.ingredientAllergies = ingredientAllergies;
	}

	public Ingredient() {
	}

	public Ingredient(String name, Boolean isDeleted, LocalDateTime createdAt) {
		this.name = name;
		this.isDeleted = isDeleted;
		this.createdAt = createdAt;
	}

	public Ingredient(Integer ingredientId, String name, Boolean isDeleted, LocalDateTime createdAt) {
		this.ingredientId = ingredientId;
		this.name = name;
		this.isDeleted = isDeleted;
		this.createdAt = createdAt;
	}

	public Integer getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Integer ingredientId) {
		this.ingredientId = ingredientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


}
