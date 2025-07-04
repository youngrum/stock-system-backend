package com.example.backend.auth.controller;

import com.example.backend.auth.contoroller.AuthController;
import com.example.backend.auth.service.AuthService;
import com.example.backend.entity.User;
import com.example.backend.exception.AuthenticationException;
import com.example.backend.security.SecurityConfig;
import com.example.backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class) // AuthController のみをロードしてテスト
@Import(SecurityConfig.class)
@DisplayName("AuthControllerのログイン機能テスト")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTPリクエストをシミュレートするためのオブジェクト

    @MockBean // AuthService のモックを作成し、Spring コンテキストに登録
    private AuthService authService;

    @MockBean // JwtUtil のモックを作成し、Spring コンテキストに登録
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper; // Java オブジェクトと JSON の変換用

    @Test
    @DisplayName("ログイン成功シナリオ")
    void testLogin_Success() throws Exception {
        // 認証サービスが返すユーザーオブジェクトを準備
        User authenticatedUser = new User();
        authenticatedUser.setUsername("testuser");
        authenticatedUser.setPassword("encodedPassword"); // 実際はハッシュ化されたパスワード
        authenticatedUser.setEnabled(true);

        // Mock の振る舞いを定義:
        // authService.authenticate が呼び出されたら、authenticatedUser を返す
        when(authService.authenticate("testuser", "password123"))
            .thenReturn(authenticatedUser);
        // jwtUtil.generateToken が呼び出されたら、モックのJWTトークンを返す
        when(jwtUtil.generateToken("testuser"))
            .thenReturn("mocked_jwt_token_for_testuser");

        // リクエストボディの作成
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(); //
        loginRequest.setUsername("testuser"); //
        loginRequest.setPassword("password123"); //

        // MockMvc を使って HTTP POST リクエストをシミュレートし、結果を検証
        mockMvc.perform(post("/v1/api/login") //
                .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-Type を JSON に設定
                .content(objectMapper.writeValueAsString(loginRequest))) // LoginRequest オブジェクトを JSON 文字列に変換してリクエストボディに設定
                .andExpect(status().isOk()) // HTTPステータスが 200 OK であることを検証
                .andExpect(jsonPath("$.status").value(200)) // レスポンスJSON の status フィールドが 200 であることを検証
                .andExpect(jsonPath("$.message").value("Login successful.")) // レスポンスJSON の message フィールドを検証
                .andExpect(jsonPath("$.data.token").value("mocked_jwt_token_for_testuser")) // レスポンスJSON の data.token フィールドを検証
                .andExpect(jsonPath("$.data.username").value("testuser")); // レスポンスJSON の data.username フィールドを検証
    }

    @Test
    @DisplayName("ログイン失敗シナリオ: 認証例外")
    void testLogin_AuthenticationFailed() throws Exception {
        // Mock の振る舞いを定義:
        // authService.authenticate が呼び出されたら AuthenticationException をスローする
        when(authService.authenticate(anyString(), anyString())) // 任意の文字列引数に対して
            .thenThrow(new AuthenticationException("Invalid username or password.")); // この例外をスロー

        // リクエストボディの作成（認証に失敗するような適当な値）
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest(); //
        loginRequest.setUsername("wronguser"); //
        loginRequest.setPassword("wrongpassword"); //

        // MockMvc を使って HTTP POST リクエストをシミュレートし、結果を検証
        mockMvc.perform(post("/v1/api/login") //
                .contentType(MediaType.APPLICATION_JSON) // リクエストのContent-Type を JSON に設定
                .content(objectMapper.writeValueAsString(loginRequest))) // LoginRequest オブジェクトを JSON 文字列に変換してリクエストボディに設定
                .andExpect(status().isUnauthorized()) // HTTPステータスが 401 Unauthorized であることを検証
                .andExpect(jsonPath("$.status").value(401)) // レスポンスJSON の status フィールドが 401 であることを検証
                .andExpect(jsonPath("$.message").value("Login failed.")) // レスポンスJSON の message フィールドを検証
                .andExpect(jsonPath("$.data.error").value("Invalid username or password.")); // レスポンスJSON の data.error フィールドを検証
    }
}