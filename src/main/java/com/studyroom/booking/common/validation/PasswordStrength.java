package com.studyroom.booking.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordStrength {

    String message() default "密码至少需要6位，且需包含大小写字母和数字";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}