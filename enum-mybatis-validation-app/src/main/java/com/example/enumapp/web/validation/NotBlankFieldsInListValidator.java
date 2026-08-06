package com.example.enumapp.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Collection;

public class NotBlankFieldsInListValidator implements ConstraintValidator<NotBlankFieldsInList, Collection<?>> {

    private String[] fields;

    @Override
    public void initialize(NotBlankFieldsInList annotation) {
        this.fields = annotation.fields();
    }

    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("list must not be null")
                    .addConstraintViolation();
            return false;
        }
        if (value.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("list must not be empty")
                    .addConstraintViolation();
            return false;
        }

        int index = 0;
        boolean valid = true;
        for (Object item : value) {
            if (item == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("list[%d] must not be null".formatted(index))
                        .addConstraintViolation();
                valid = false;
                index++;
                continue;
            }
            BeanWrapperImpl wrapper = new BeanWrapperImpl(item);
            for (String field : fields) {
                Object fieldValue = wrapper.getPropertyValue(field);
                if (fieldValue == null || (fieldValue instanceof String s && s.isBlank())) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                                    "list[%d].%s must not be null or blank".formatted(index, field)
                            )
                            .addConstraintViolation();
                    valid = false;
                }
            }
            index++;
        }
        return valid;
    }
}
