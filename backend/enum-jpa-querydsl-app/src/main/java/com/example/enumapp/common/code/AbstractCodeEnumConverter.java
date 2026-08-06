package com.example.enumapp.common.code;

import jakarta.persistence.AttributeConverter;

/**
 * CodeEnum → DB code 문자열 공통 변환기.
 * 구체 Enum 별로 하위 클래스를 두고 {@code @Converter(autoApply = true)} 로 등록한다.
 */
public abstract class AbstractCodeEnumConverter<E extends Enum<E> & CodeEnum>
        implements AttributeConverter<E, String> {

    private final Class<E> type;

    protected AbstractCodeEnumConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return CodeEnums.toCode(attribute);
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return CodeEnums.fromCode(type, dbData);
    }
}
