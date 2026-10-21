package com.recipebox.App.entity;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import com.recipebox.App.converter.BmiCategoryConverter;
import com.recipebox.App.enums.BMICategory;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bmi_records")
public class BmiRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bmi_id")
	private int bmiId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private double bmiValue;

	@Convert(converter = BmiCategoryConverter.class)
	private BMICategory bmiCategory;

	@NotNull(message = "Age is required")
	@Min(value = 1, message = "Age must be at least 1")
	private Integer age;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotNull(message = "Height is required")
	@DecimalMin(value = "0.1", message = "Height must be greater than 0")
	private Double height;

	@NotNull(message = "Weight is required")
	@DecimalMin(value = "0.01", message = "Weight must be greater than 0")
	private Double weight;

	private boolean isCurrent;

	private LocalDateTime recordedAt;
	
	@Transient
	private Double feet;

	@Transient
	private Double heightInches;

	public BmiRecord() {
	}

	public BmiRecord(int bmiId, User user, double bmiValue, BMICategory bmiCategory, Integer age, String gender,
			Double height, Double weight, boolean isCurrent, LocalDateTime recordedAt) {
		this.bmiId = bmiId;
		this.user = user;
		this.bmiValue = bmiValue;
		this.bmiCategory = bmiCategory;
		this.age = age;
		this.gender = gender;
		this.height = height;
		this.weight = weight;
		this.isCurrent = isCurrent;
		this.recordedAt = recordedAt;
	}

	public BmiRecord(User user, double bmiValue, BMICategory bmiCategory, Integer age, String gender, Double height,
			Double weight, boolean isCurrent, LocalDateTime recordedAt) {
		this.user = user;
		this.bmiValue = bmiValue;
		this.bmiCategory = bmiCategory;
		this.age = age;
		this.gender = gender;
		this.height = height;
		this.weight = weight;
		this.isCurrent = isCurrent;
		this.recordedAt = recordedAt;
	}

	public int getBmiId() {
		return bmiId;
	}

	public void setBmiId(int bmiId) {
		this.bmiId = bmiId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public double getBmiValue() {
		return bmiValue;
	}

	public void setBmiValue(double bmiValue) {
		this.bmiValue = bmiValue;
	}

	public BMICategory getBmiCategory() {
		return bmiCategory;
	}

	public void setBmiCategory(BMICategory bmiCategory) {
		this.bmiCategory = bmiCategory;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(LocalDateTime recordedAt) {
		this.recordedAt = recordedAt;
	}

	public Double getFeet() {
		return feet;
	}

	public void setFeet(Double feet) {
		this.feet = feet;
	}

	public Double getHeightInches() {
		return heightInches;
	}

	public void setHeightInches(Double heightInches) {
		this.heightInches = heightInches;
	}

}
