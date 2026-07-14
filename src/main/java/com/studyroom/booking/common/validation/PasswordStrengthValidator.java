package com.studyroom.booking.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordStrengthValidator implements ConstraintValidator<PasswordStrength, String> {

    private static final Pattern HAS_UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return true;
        }

        if (password.length() < 6) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("密码至少需要6位")
                    .addConstraintViolation();
            return false;
        }

        boolean hasUpperCase = HAS_UPPER_CASE.matcher(password).find();
        boolean hasLowerCase = HAS_LOWER_CASE.matcher(password).find();
        boolean hasDigit = HAS_DIGIT.matcher(password).find();

        if (!hasUpperCase || !hasLowerCase || !hasDigit) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("密码需包含大小写字母和数字")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}