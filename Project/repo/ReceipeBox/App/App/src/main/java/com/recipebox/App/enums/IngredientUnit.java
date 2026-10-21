package com.recipebox.App.enums;

public enum IngredientUnit {
	GRAM("gram"),
	KG("kg"),
	ML("ml"),
	TSP("tsp"),
	TBSP("tbsp"),
	PCS("pcs");

	private final String value;

	IngredientUnit(String value) {
        this.value = value;
    }

	public String getValue() {
		return value;
	}

	public static IngredientUnit fromValue(String text) {
		for (IngredientUnit u : IngredientUnit.values()) {
			if (u.name().equalsIgnoreCase(text) || u.value.equalsIgnoreCase(text)) {
				return u;
			}
		}
		return GRAM;
	}
}