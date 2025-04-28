package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

// リクエストボディ用のDTO
class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    // --- ゲッター・セッター ---
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

// レスポンス用DTO
class MessageResponse {
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

@RestController
@RequestMapping("/v1/api")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // ここは仮実装（実際はDBや認証サーバーで確認する）
        if ("tanaka".equals(username) && "password123".equals(password)) {
            return ResponseEntity.ok(new MessageResponse("Login successful."));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username or password."));
        }
    }
}
