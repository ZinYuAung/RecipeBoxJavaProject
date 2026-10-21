package com.recipebox.App.converter;

import com.recipebox.App.enums.MealType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // autoApply = true tells JPA to automatically use this for all MealType properties
public class MealTypeConverter implements AttributeConverter<MealType, String> {

    // Converts Java Enum to Database String (e.g., DINNER -> "Dinner")
    @Override
    public String convertToDatabaseColumn(MealType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    // Converts Database String to Java Enum (e.g., "Dinner" -> DINNER)
    @Override
    public MealType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return MealType.fromValue(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}