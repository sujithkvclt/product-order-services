package com.ecommerce.productorder.validator;

import com.ecommerce.productorder.model.enums.UserRole;
import com.ecommerce.productorder.validator.annotation.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.stream.Stream;

public class RoleValidator implements ConstraintValidator<Role, String> {

    UserRole[] enumValues;

    @Override
    public void initialize(Role constraintAnnotation) {
        enumValues = constraintAnnotation.anyOf();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || Stream.of(enumValues).anyMatch(v -> v.name().equalsIgnoreCase(value));
    }
}
