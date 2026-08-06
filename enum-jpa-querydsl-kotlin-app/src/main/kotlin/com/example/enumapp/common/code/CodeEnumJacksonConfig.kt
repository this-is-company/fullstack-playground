package com.example.enumapp.common.code

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Request/Response JSON 에서 CodeEnum 을 code 문자열로 직렬화/역직렬화한다.
 * null, "", "   " 는 null 로 변환한다.
 */
@Configuration
class CodeEnumJacksonConfig {

    @Bean
    fun codeEnumJacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            val module = SimpleModule("CodeEnumModule")
            module.setDeserializerModifier(object : BeanDeserializerModifier() {
                override fun modifyEnumDeserializer(
                    config: DeserializationConfig,
                    type: com.fasterxml.jackson.databind.JavaType,
                    beanDesc: BeanDescription,
                    deserializer: JsonDeserializer<*>
                ): JsonDeserializer<*> {
                    val raw = type.rawClass
                    if (raw.isEnum && CodeEnum::class.java.isAssignableFrom(raw)) {
                        return CodeEnumDeserializer(raw)
                    }
                    return deserializer
                }
            })
            module.setSerializerModifier(object : BeanSerializerModifier() {
                override fun modifyEnumSerializer(
                    config: com.fasterxml.jackson.databind.SerializationConfig,
                    valueType: com.fasterxml.jackson.databind.JavaType,
                    beanDesc: BeanDescription,
                    serializer: JsonSerializer<*>
                ): JsonSerializer<*> {
                    if (CodeEnum::class.java.isAssignableFrom(valueType.rawClass)) {
                        return CodeEnumSerializer()
                    }
                    return serializer
                }
            })
            builder.modulesToInstall(module)
        }

    internal class CodeEnumSerializer : JsonSerializer<CodeEnum>() {
        override fun serialize(value: CodeEnum?, gen: JsonGenerator, serializers: SerializerProvider) {
            if (value == null) {
                gen.writeNull()
            } else {
                gen.writeString(value.code)
            }
        }
    }

    /**
     * Java 의 raw-type deserializer 와 동일하게 Class<*> 를 받는다.
     * (Kotlin 은 Enum&lt;E&gt; & CodeEnum 교차 제네릭을 호출부에 추론하기 어렵다)
     */
    internal class CodeEnumDeserializer(
        private val type: Class<*>
    ) : JsonDeserializer<Any?>() {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any? {
            val code = p.valueAsString
            return fromCodeUnchecked(type, code)
        }

        override fun getNullValue(ctxt: DeserializationContext): Any? = null

        @Suppress("UNCHECKED_CAST")
        private fun fromCodeUnchecked(type: Class<*>, code: String?): Any? {
            if (code.isNullOrBlank()) {
                return null
            }
            val constants = type.enumConstants as Array<out CodeEnum>
            return constants.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException(
                    "Unknown code '%s' for enum %s".format(code, type.simpleName)
                )
        }
    }
}
