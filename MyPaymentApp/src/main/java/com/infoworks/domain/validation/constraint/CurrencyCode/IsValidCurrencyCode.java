package com.infoworks.domain.validation.constraint.CurrencyCode;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = CurrencyCodeConstraint.class)
public @interface IsValidCurrencyCode {
    String message() default "e.g. BDT, USD, EUR, CAD etc";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
