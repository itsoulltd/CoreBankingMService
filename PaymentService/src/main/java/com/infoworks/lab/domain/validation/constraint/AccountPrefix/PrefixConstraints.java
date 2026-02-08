package com.infoworks.lab.domain.validation.constraint.AccountPrefix;

import com.infoworks.lab.domain.types.AccountPrefix;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PrefixConstraints implements ConstraintValidator<IsValidPrefix, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        if (!value.isEmpty()){
            try {
                return AccountPrefix.valueOf(value) != null;
            } catch (IllegalArgumentException e) {}
        }
        return false;
    }

}
