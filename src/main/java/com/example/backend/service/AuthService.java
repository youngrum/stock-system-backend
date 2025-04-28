package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.backend.exception.AuthenticationException;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String password) throws AuthenticationException {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = optionalUser.get();

        // パスワード照合（平文で比較。あとでハッシュ比較に変更予定）
        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        // ユーザーが無効化されていたら拒否する
        if (!user.isEnabled()) {
            throw new AuthenticationException("User account is disabled.");
        }

        return user;
    }
}
