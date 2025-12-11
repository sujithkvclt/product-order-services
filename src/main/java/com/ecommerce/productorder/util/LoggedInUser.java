package com.ecommerce.productorder.util;

import com.ecommerce.productorder.exception.ApplicationException;
import com.ecommerce.productorder.model.entity.User;
import com.ecommerce.productorder.model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class LoggedInUser {

    public static Long getId() {
        return get().getId();
    }

    public static User get() {
        return Optional.ofNullable(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication).map(
                Authentication::getPrincipal).map(User.class::cast).orElseThrow(
                () -> new ApplicationException("Unable to load user from security context"));
    }

    public static boolean isAdmin() {
        return get().getRole().equals(UserRole.ADMIN);
    }

    public static boolean isUser() {
        return get().getRole().equals(UserRole.USER);
    }

    public static boolean isPremiumUser() {
        return get().getRole().equals(UserRole.PREMIUM_USER);
    }
}
