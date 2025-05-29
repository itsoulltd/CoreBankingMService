package com.infoworks.lab.domain.validation.constraint.AccountType;

import com.infoworks.lab.domain.types.AccountType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AccountTypeConstraints implements ConstraintValidator<IsValidAccountType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        if (!value.isEmpty()){
            try {
                return AccountType.valueOf(value) != null;
            } catch (IllegalArgumentException e) {}
        }
        return false;
    }

}
