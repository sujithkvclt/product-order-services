package com.ecommerce.productorder.service.impl;

import com.ecommerce.productorder.exception.DuplicateResourceException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.model.dto.response.AuthResponse;
import com.ecommerce.productorder.model.dto.request.LoginRequest;
import com.ecommerce.productorder.model.dto.request.RegisterRequest;
import com.ecommerce.productorder.model.entity.User;
import com.ecommerce.productorder.model.enums.UserRole;
import com.ecommerce.productorder.repository.UserRepository;
import com.ecommerce.productorder.security.JwtUtil;
import com.ecommerce.productorder.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        validatedDuplicateUser(request);
        User user = buildUser(request);
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        String token = jwtUtil.generateToken(savedUser);

        return buildResponse(token, savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        User user = getAuthenticatedUser(request);
        String token = jwtUtil.generateToken(user);
        log.info("User authenticated successfully: {}", user.getUsername());

        return buildResponse(token, user);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private User getAuthenticatedUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        return user;
    }

    private static AuthResponse buildResponse(String token, User savedUser) {
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    private User buildUser(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.valueOf(request.getRole().toUpperCase()))
                .enabled(true)
                .build();
    }

    private void validatedDuplicateUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
    }
}
