package com.infoworks.lab.domain.validation.constraint.AccountType;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = AccountTypeConstraints.class)
public @interface IsValidAccountType {
    String message() default "e.g. MASTER or USER or CURRENT or SAVING";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
