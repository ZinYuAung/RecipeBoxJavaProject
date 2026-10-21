package com.recipebox.App.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "favorites")
public class Favorites {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int favorite_id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@ManyToOne
	@JoinColumn(name="recipe_id")
	private Recipe recipe;
	
	@Column(name = "created_at", updatable = false, insertable = false)
	private LocalDateTime createdAt;
	
	public Favorites() {}
	
	public Favorites(int favoriteId, User userId, Recipe recipeId,LocalDateTime createdAt) {
		super();
		this.favorite_id = favoriteId;
		this.user = userId;
		this.recipe = recipeId;
		this.createdAt = createdAt;
	}
	
	public int getFavorite_id() {
		return favorite_id;
	}

	public void setFavorite_id(int favorite_id) {
		this.favorite_id = favorite_id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	
	public User getUser() {
		return user;
	}

	public void setUser(User userId) {
		this.user = userId;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipeId) {
		this.recipe = recipeId;
	}
	
}
