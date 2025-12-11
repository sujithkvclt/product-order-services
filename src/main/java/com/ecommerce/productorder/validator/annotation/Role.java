package com.ecommerce.productorder.validator.annotation;

import com.ecommerce.productorder.model.enums.UserRole;
import com.ecommerce.productorder.validator.RoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Constraint(validatedBy = RoleValidator.class)
public @interface Role {

    UserRole[] anyOf() default {UserRole.ADMIN, UserRole.USER, UserRole.PREMIUM_USER,};

    String message() default "must be any of {anyOf}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
