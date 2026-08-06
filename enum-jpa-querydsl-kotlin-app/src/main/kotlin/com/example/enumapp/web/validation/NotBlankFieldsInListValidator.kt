package com.example.enumapp.web.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.BeanWrapperImpl

class NotBlankFieldsInListValidator : ConstraintValidator<NotBlankFieldsInList, Collection<*>?> {

    private lateinit var fields: Array<String>

    override fun initialize(annotation: NotBlankFieldsInList) {
        fields = annotation.fields
    }

    override fun isValid(value: Collection<*>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("list must not be null")
                .addConstraintViolation()
            return false
        }
        if (value.isEmpty()) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("list must not be empty")
                .addConstraintViolation()
            return false
        }

        var index = 0
        var valid = true
        for (item in value) {
            if (item == null) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate("list[%d] must not be null".format(index))
                    .addConstraintViolation()
                valid = false
                index++
                continue
            }
            val wrapper = BeanWrapperImpl(item)
            for (field in fields) {
                val fieldValue = wrapper.getPropertyValue(field)
                if (fieldValue == null || (fieldValue is String && fieldValue.isBlank())) {
                    context.disableDefaultConstraintViolation()
                    context.buildConstraintViolationWithTemplate(
                        "list[%d].%s must not be null or blank".format(index, field),
                    )
                        .addConstraintViolation()
                    valid = false
                }
            }
            index++
        }
        return valid
    }
}
