package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // ResourceNotFoundException を捕捉し、404レスポンスを返す
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
        Map.of(
            "status", 404,
            "message", ex.getMessage(),
            "error", "Resource not found",
            "timestamp", LocalDateTime.now().toString()));
  }

  // 任意：すべての予期しない例外を捕捉（テスト用）
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    ex.printStackTrace(); // 開発中はログ出力推奨
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        Map.of(
            "status", 500,
            "message", "サーバー内部で予期せぬエラーが発生しました",
            "error", ex.getClass().getSimpleName(),
            "timestamp", LocalDateTime.now().toString()));
  }
}
