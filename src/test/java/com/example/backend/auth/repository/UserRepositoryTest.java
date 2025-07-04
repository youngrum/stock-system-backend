package com.example.backend.auth.repository;

import com.example.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // JPAコンポーネントのテストに特化したアノテーション
@DisplayName("UserRepositoryのDB疎通機能テスト")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager; // テスト用のエンティティマネージャー

    private User testUser;

    @BeforeEach
    void setUp() {
        // 各テストの前にデータを準備
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEnabled(true);
        entityManager.persistAndFlush(testUser); // データベースに保存
    }

    @Test
    @DisplayName("成功：ユーザーが存在する場合のテスト")
    void testFindByUsername_UserExists() {
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        assertThat(foundUser).isPresent(); // Optionalに値が含まれていることを確認
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser"); // ユーザー名が正しいことを確認
        assertThat(foundUser.get().getPassword()).isEqualTo("password123"); // パスワードが正しいことを確認
    }

    @Test
    @DisplayName("失敗：ユーザーが存在しない場合のテスト")
    void testFindByUsername_UserNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");

        assertThat(foundUser).isEmpty(); // Optionalが空であることを確認
    }
}