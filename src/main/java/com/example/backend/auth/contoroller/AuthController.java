package com.example.backend.auth.contoroller;

import com.example.backend.auth.service.AuthService;
import com.example.backend.entity.User;
import com.example.backend.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.example.backend.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "認証API", description = "ログイン/ログアウト機能")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "ログイン認証")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User authenticatedUser = authService.authenticate(request.getUsername(), request.getPassword());
            String token = jwtUtil.generateToken(authenticatedUser.getUsername());
            System.out.println(authenticatedUser.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", authenticatedUser.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Login successful.");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 401);
            error.put("message", "Login failed.");
            error.put("data", Map.of("error", e.getMessage()));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 401);
            error.put("message", "Unauthorized");
            error.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String username = authentication.getName();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("roles", new ArrayList<>()); // 今後必要ならロールも取得可能

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "User fetched successfully.");
        response.put("data", userData);

        return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
}


