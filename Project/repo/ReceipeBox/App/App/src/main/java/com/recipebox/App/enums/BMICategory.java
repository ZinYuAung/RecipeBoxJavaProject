package com.recipebox.App.enums;

public enum BMICategory {

	UNDERWEIGHT(550, 850),
    NORMAL(400, 700),
    OVERWEIGHT(300, 500),
    OBESE(250, 450);

	private final int minMealCalories;
	private final int maxMealCalories;

	BMICategory(int minMealCalories, int maxMealCalories) {
		this.minMealCalories = minMealCalories;
		this.maxMealCalories = maxMealCalories;
	}

	public int getMinMealCalories() {
		return minMealCalories;
	}

	public int getMaxMealCalories() {
		return maxMealCalories;
	}

	// Helper method to convert String safely
	public static BMICategory fromString(String category) {
		if (category == null || category.isBlank()) {
			return NORMAL;
		}
		try {
			return BMICategory.valueOf(category.toUpperCase().trim());
		} catch (IllegalArgumentException e) {
			return NORMAL;
		}
	}
}