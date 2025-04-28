package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // usernameでユーザーを検索するメソッド（JPAが自動実装してくれる）
    Optional<User> findByUsername(String username);
}
