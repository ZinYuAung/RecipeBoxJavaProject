package com.recipebox.App.converter;

import com.recipebox.App.enums.BMICategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BmiCategoryConverter implements AttributeConverter<BMICategory, String> {

    @Override
    public String convertToDatabaseColumn(BMICategory attribute) {
        return (attribute == null) ? null : attribute.name();
    }

    @Override
    public BMICategory convertToEntityAttribute(String dbData) {
       
        return BMICategory.fromString(dbData);
    }
}