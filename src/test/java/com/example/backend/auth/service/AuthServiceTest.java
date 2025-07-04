package com.example.backend.auth.service;

import com.example.backend.auth.repository.UserRepository;
import com.example.backend.entity.User;
import com.example.backend.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Mockitoを使用するためのExtension
@DisplayName("AuthServiceの認証機能テスト")
class AuthServiceTest {

    @Mock // UserRepositoryのモックを作成
    private UserRepository userRepository;

    @InjectMocks // AuthServiceにモックがインジェクトされる
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEnabled(true);
    }

    @Test
    @DisplayName("認証成功: 有効なユーザー名とパスワード")
    void testAuthenticate_Success() throws AuthenticationException {
        // userRepository.findByUsernameがtestUserを返すようにモックの振る舞いを定義
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User authenticatedUser = authService.authenticate("testuser", "password123");

        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("認証失敗: ユーザーが見つからない場合")
    void testAuthenticate_UserNotFound() {
        // ユーザーが見つからない場合のモックの振る舞いを定義
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // AuthenticationExceptionがスローされることを検証
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("nonexistent", "anypassword");
        });

        assertThat(exception.getMessage()).isEqualTo("Invalid username or password.");
    }

    @Test
    @DisplayName("認証失敗: パスワードが一致しない場合")
    void testAuthenticate_PasswordMismatch() {
        // ユーザーが見つかるがパスワードが一致しない場合のモックの振る舞いを定義
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", "wrongpassword");
        });

        assertThat(exception.getMessage()).isEqualTo("Invalid username or password.");
    }

    @Test
    @DisplayName("認証失敗: ユーザーアカウントが無効化されている場合")
    void testAuthenticate_UserDisabled() {
        // ユーザーが無効化されている場合のテスト
        testUser.setEnabled(false); // ユーザーを無効化
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", "password123");
        });

        assertThat(exception.getMessage()).isEqualTo("User account is disabled.");
    }
}