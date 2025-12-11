package com.ecommerce.productorder.service;

import com.ecommerce.productorder.model.dto.response.AuthResponse;
import com.ecommerce.productorder.model.dto.request.LoginRequest;
import com.ecommerce.productorder.model.dto.request.RegisterRequest;
import com.ecommerce.productorder.model.entity.User;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    User getUserByUsername(String username);
}
