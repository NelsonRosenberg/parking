package com.spothero.parking.dtos.validations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.time.ZoneId;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidTimeZoneValidator.class)
public @interface TimeZoneFormat {

    String message() default "Invalid Timezone";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class ValidTimeZoneValidator implements ConstraintValidator<TimeZoneFormat, String> {

    @Override
    public void initialize(final TimeZoneFormat constraintAnnotation) {
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return value == null || ZoneId.getAvailableZoneIds().contains(value);
    }
}
