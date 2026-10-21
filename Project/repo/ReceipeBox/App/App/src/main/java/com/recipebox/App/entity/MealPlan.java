package com.recipebox.App.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.recipebox.App.converter.BmiCategoryConverter;
import com.recipebox.App.enums.BMICategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_plans")
public class MealPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer planId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Convert(converter = BmiCategoryConverter.class)
	private BMICategory bmiCategory;

	private Boolean isActive = false;

	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MealPlanItem> items;

	public MealPlan() {
	}

	public MealPlan(Integer planId, User user, BMICategory bmiCategory, LocalDateTime createdAt) {
		this.planId = planId;
		this.user = user;
		this.bmiCategory = bmiCategory;
		this.createdAt = createdAt;
	}

	public MealPlan(User user, BMICategory bmiCategory, LocalDateTime createdAt) {
		this.user = user;
		this.bmiCategory = bmiCategory;
		this.createdAt = createdAt;
	}

	public Integer getPlanId() {
		return planId;
	}

	public void setPlanId(Integer planId) {
		this.planId = planId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BMICategory getBmiCategory() {
		return bmiCategory;
	}

	public void setBmiCategory(BMICategory bmiCategory) {
		this.bmiCategory = bmiCategory;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<MealPlanItem> getItems() {
		return items;
	}

	public void setItems(List<MealPlanItem> items) {
		this.items = items;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
