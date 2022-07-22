package com.infoworks.lab.domain.validation.constraint.MoneyFormat;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = MoneyConstraint.class)
public @interface Money {
    String message() default "Money must be well formatted. " +
            "e.g. 0.00 or Any number digits before precision and least 2 digit after precision.";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
