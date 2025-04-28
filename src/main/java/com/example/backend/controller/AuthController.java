package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.AuthService;
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

    // ビジネスロジックファイル
    private final AuthService authService;
    // トークン発行ファイル
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
            // AuthServiceを使って認証チェック
            User authenticatedUser = authService.authenticate(request.getUsername(), request.getPassword());

            // 認証成功時
            Map<String, Object> response = new HashMap<>();
            String token = jwtUtil.generateToken(authenticatedUser.getUsername()); // トークン発行
            response.put("message", "Login successful.");
            response.put("token", token); // トークンをレスポンスに含める

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // 認証失敗
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // ユーザー名を取得
        String username = authentication.getName();
        // roleを取得
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("roles", new ArrayList<>()); // 権限取得するなら、ここを拡張できる

        return ResponseEntity.ok(response);
    }

    // リクエストボディの受け皿クラス（内部クラス）
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