package com.infoworks.lab.domain.validation.constraint.AccountPrefix;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = PrefixConstraints.class)
public @interface IsValidPrefix {
    String message() default "e.g. CASH, REVENUE, bKash, BANK";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
