package com.example.backend.auth.service;

import com.example.backend.auth.repository.UserRepository;
import com.example.backend.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.backend.exception.AuthenticationException;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String password) throws AuthenticationException {
        log.debug("Authentication attempt for username: ", username);
        
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            log.debug("User not found: ", username);
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = optionalUser.get();
        String dbPassword = user.getPassword();
        
        // デバッグログ（本番では削除）
        System.out.println("Input password:"+ password);
        System.out.println("DB password:"+ dbPassword);
        System.out.println("Input password length:"+ password != null ? password.length() : "null");
        System.out.println("DB password length:"+ dbPassword != null ? dbPassword.length() : "null");
        System.out.println("Password equals:"+ password != null && password.equals(dbPassword));

        // パスワード照合（平文で比較。あとでハッシュ比較に変更予定）
        if (!password.equals(dbPassword)) {
            System.out.println("Password mismatch for user:"+ username);
            throw new AuthenticationException("Invalid username or password.");
        }

        // ユーザーが無効化されていたら拒否する
        if (!user.isEnabled()) {
            System.out.println("User account disabled:"+ username);
            throw new AuthenticationException("User account is disabled.");
        }

        System.out.println("Authentication successful for user:"+ username);
        return user;
    }
}