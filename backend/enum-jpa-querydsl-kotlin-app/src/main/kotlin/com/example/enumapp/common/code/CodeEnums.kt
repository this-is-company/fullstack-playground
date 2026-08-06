package com.example.enumapp.common.code

object CodeEnums {

    @JvmStatic
    fun <E> fromCode(type: Class<E>, code: String?): E?
        where E : Enum<E>, E : CodeEnum {
        if (code.isNullOrBlank()) {
            return null
        }
        return type.enumConstants
            .firstOrNull { it.code == code }
            ?: throw IllegalArgumentException(
                "Unknown code '%s' for enum %s".format(code, type.simpleName)
            )
    }

    @JvmStatic
    fun toCode(value: CodeEnum?): String? = value?.code
}
