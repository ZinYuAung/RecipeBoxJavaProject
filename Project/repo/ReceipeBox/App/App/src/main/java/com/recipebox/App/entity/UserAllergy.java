package com.recipebox.App.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="user_allergies")
public class UserAllergy {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@ManyToOne
	@JoinColumn(name="allergen_id")
	private Allergy allergen;

	public UserAllergy(int id, User user, Allergy allergen) {
		super();
		this.id = id;
		this.user = user;
		this.allergen = allergen;
	}

	public UserAllergy() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Allergy getAllergen() {
		return allergen;
	}

	public void setAllergen(Allergy allergen) {
		this.allergen = allergen;
	}
	
	
}
