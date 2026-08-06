package com.example.enumapp.common.code;

import java.util.Arrays;
import java.util.Objects;

public final class CodeEnums {

    private CodeEnums() {
    }

    public static <E extends Enum<E> & CodeEnum> E fromCode(Class<E> type, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(type.getEnumConstants())
                .filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown code '%s' for enum %s".formatted(code, type.getSimpleName())
                ));
    }

    public static String toCode(CodeEnum value) {
        return value == null ? null : value.getCode();
    }
}
