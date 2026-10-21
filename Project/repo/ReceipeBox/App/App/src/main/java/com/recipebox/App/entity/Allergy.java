package com.recipebox.App.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "allergens")
public class Allergy {
	// data
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "allergen_id")
	private int  allergenId;
	

	@NotBlank(message = "Allergy name cannot be blank.")
	@Size(max = 100, message = "Allergen name must not exceed 100 characters.")
	@Pattern(regexp = "^$|.*[a-zA-Zа-яА-ЯğüşöçİĞÜŞÖÇ].*", message = "Allergy name cannot be only numbers or invalid characters.")
    private String name; 
	
	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	public Allergy() { } 

	// constructor
	public Allergy(int allergenId, String name,Boolean isDeleted, LocalDateTime createdAt) {
		super();
		this.allergenId = allergenId;
		this.name = name;
		this.isDeleted = isDeleted;
		this.createdAt = createdAt;
	}

	public int getAllergenId() {
		return allergenId;
	}

	public void setAllergenId(int allergenId) {
		this.allergenId = allergenId;
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
