package com.infoworks.lab.domain.validation.constraint.MoneyFormat;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MoneyConstraint implements ConstraintValidator<Money, String> {

    /**
     * Since regular expressions are horrible to read, much less understand, here is the verbose equivalent:
     *
     * ^                         # Start of string
     *  [0-9]+                   # Require one or more numbers
     *        (                  # Begin optional group
     *         \.                # Point must be escaped or it is treated as "any character"
     *           [0-9]{1,2}      # One or two numbers
     *                     )?    # End group--signify that it's optional with "?"
     *                       $   # End of string
     *
     */

    /**
     * valid = ["123.12", "2", "56754", "92929292929292.12", "0.21", "3.1"]
     * invalid = ["12.1232", "2.23332", "e666.76"]
     */
    private static final String REGEX_1 = "^[0-9]+(\\.[0-9]{1,2})?$";

    /**
     * valid = ["123.12", "92929292929292.12", "0.21"]
     * invalid = ["12.1232", "2.23332", "e666.76", "2", "3.1", "56754"]
     */
    private static final String REGEX_2 = "^[0-9]+\\.[0-9]{2}?$";

    @Override
    public void initialize(Money constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!value.isEmpty()){
            return value.matches(REGEX_2);
        }
        return false;
    }
}
