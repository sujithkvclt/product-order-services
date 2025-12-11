package com.ecommerce.productorder.model.dto.response;

import com.ecommerce.productorder.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
}
