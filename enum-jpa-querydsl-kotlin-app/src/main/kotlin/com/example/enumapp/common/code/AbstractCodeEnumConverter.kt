package com.example.enumapp.common.code

import jakarta.persistence.AttributeConverter

/**
 * CodeEnum → DB code 문자열 공통 변환기.
 * 구체 Enum 별로 하위 클래스를 두고 `@Converter(autoApply = true)` 로 등록한다.
 */
abstract class AbstractCodeEnumConverter<E>(
    private val type: Class<E>
) : AttributeConverter<E, String>
    where E : Enum<E>, E : CodeEnum {

    override fun convertToDatabaseColumn(attribute: E?): String? =
        CodeEnums.toCode(attribute)

    override fun convertToEntityAttribute(dbData: String?): E? =
        CodeEnums.fromCode(type, dbData)
}
