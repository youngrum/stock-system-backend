package com.example.backend.security;

import com.example.backend.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()); // .csrf(...) の呼び出しが cors() の直後にあることで、戻り値の型が違うためチェーンさせない
        return http
            .csrf(csrf -> csrf.disable()) // CSRF無効化（RESTFUL APIは基本オフでOK）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // セッション使わない
            .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", 
                "/login", 
                "inventory", 
                "inventory/**", 
                "order", 
                "order/**", 
                "/404.html", 
                "/favicon.ico", 
                "/*.html", 
                "/*.txt", 
                "/*.svg", 
                "/*.png", 
                "/_next/**", 
                "/v1/api/login", 
                "/v1/api/logout",
                "/v3/api-docs/**",
                "/swagger-ui/**", 
                "/swagger-ui.html",
                "/h2-console/**"
            ).permitAll()
            .anyRequest().authenticated()
        ).headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())) // H2 Console表示許可（ブラウザのX-Frame制御）
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class) // JWTフィルターを追加
        .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ★ グローバル CORS 設定の Bean を定義
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // フロントからのアクセスを許可
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // JWTなどの認証情報も許可

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
