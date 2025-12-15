package com.ecommerce.productorder.config;

import com.ecommerce.productorder.model.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }

        return Optional.empty();
    }
}
