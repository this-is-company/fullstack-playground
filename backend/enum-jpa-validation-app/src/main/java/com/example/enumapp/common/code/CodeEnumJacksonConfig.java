package com.example.enumapp.common.code;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Request/Response JSON 에서 CodeEnum 을 code 문자열로 직렬화/역직렬화한다.
 * null, "", "   " 는 null 로 변환한다.
 */
@Configuration
public class CodeEnumJacksonConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer codeEnumJacksonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("CodeEnumModule");
            module.setDeserializerModifier(new BeanDeserializerModifier() {
                @Override
                public JsonDeserializer<?> modifyEnumDeserializer(
                        DeserializationConfig config,
                        com.fasterxml.jackson.databind.JavaType type,
                        BeanDescription beanDesc,
                        JsonDeserializer<?> deserializer
                ) {
                    Class<?> raw = type.getRawClass();
                    if (raw.isEnum() && CodeEnum.class.isAssignableFrom(raw)) {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) raw;
                        return new CodeEnumDeserializer(enumClass);
                    }
                    return deserializer;
                }
            });
            module.setSerializerModifier(new BeanSerializerModifier() {
                @Override
                public JsonSerializer<?> modifyEnumSerializer(
                        com.fasterxml.jackson.databind.SerializationConfig config,
                        com.fasterxml.jackson.databind.JavaType valueType,
                        BeanDescription beanDesc,
                        JsonSerializer<?> serializer
                ) {
                    if (CodeEnum.class.isAssignableFrom(valueType.getRawClass())) {
                        return new CodeEnumSerializer();
                    }
                    return serializer;
                }
            });
            builder.modulesToInstall(module);
        };
    }

    static final class CodeEnumSerializer extends JsonSerializer<CodeEnum> {
        @Override
        public void serialize(CodeEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.getCode());
            }
        }
    }

    static final class CodeEnumDeserializer<E extends Enum<E> & CodeEnum> extends JsonDeserializer<E> {
        private final Class<E> type;

        CodeEnumDeserializer(Class<E> type) {
            this.type = type;
        }

        @Override
        public E deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String code = p.getValueAsString();
            return CodeEnums.fromCode(type, code);
        }

        @Override
        public E getNullValue(DeserializationContext ctxt) {
            return null;
        }
    }
}
