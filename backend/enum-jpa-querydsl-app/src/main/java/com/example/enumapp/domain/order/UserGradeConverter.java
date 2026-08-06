package com.example.enumapp.domain.order;

import com.example.enumapp.common.code.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserGradeConverter extends AbstractCodeEnumConverter<UserGrade> {
    public UserGradeConverter() {
        super(UserGrade.class);
    }
}
