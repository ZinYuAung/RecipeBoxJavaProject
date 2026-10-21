package com.recipebox.App.enums;

public enum MealType {
	BREAKFAST("Breakfast"),
	LUNCH("Lunch"),
	DINNER("Dinner"),
	SNACK("Snack");

	private final String value;

	MealType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static MealType fromValue(String value) {
		for (MealType type : MealType.values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown meal type: " + value);
	}
}
