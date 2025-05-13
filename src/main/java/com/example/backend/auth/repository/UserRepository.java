package com.example.backend.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // usernameでユーザーを検索するメソッド（JPAが自動実装してくれる）
    // User = User.javaで定義した「DBのusersテーブル」と紐づくエンティティクラス
    // nullを直接返すのではなく、Optional.empty()として返す
    Optional<User> findByUsername(String username);
}
