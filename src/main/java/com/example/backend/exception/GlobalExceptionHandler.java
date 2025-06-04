package com.example.backend.exception;

import org.springframework.dao.DataIntegrityViolationException; // 追加
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // 追加
import org.springframework.web.bind.MissingServletRequestParameterException; // 追加
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException; // 追加

import java.time.LocalDateTime;
import java.util.HashMap; // Map.of はImmutableMapのため、変更可能なHashMapを使用
import java.util.Map;
import java.util.List; // バリデーションエラーの詳細用
import java.util.stream.Collectors; // バリデーションエラーの詳細用

// (jakarta.validation.ValidationException は Spring の MethodArgumentNotValidException とは別の例外体系なので注意が必要です。
// もしサービス層でValidationExceptionをスローするなら、それも捕捉すべきです)
import jakarta.validation.ValidationException; // InventoryService でスローしているため追加

@RestControllerAdvice
public class GlobalExceptionHandler {

  // ResourceNotFoundException を捕捉し、404レスポンスを返す
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("message", ex.getMessage());
    body.put("error", "Resource not found");
    body.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  // バリデーションエラー (Controllerの@RequestBodyや@Validで発生)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.toList());

    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("message", "入力値の検証に失敗しました");
    body.put("errors", errors); // どのフィールドがエラーか詳細情報を含める
    body.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // Service層でスローされる ValidationException (InventoryServiceで使用)
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, Object>> handleServiceValidationException(ValidationException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("message", ex.getMessage());
    body.put("error", "Validation Error");
    body.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }


  // データベースの整合性違反 (例: NOT NULL制約違反、UNIQUE制約違反)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
      Map<String, Object> body = new HashMap<>();
      body.put("status", HttpStatus.CONFLICT.value()); // または BAD_REQUEST (400)
      body.put("message", "データベースの整合性違反が発生しました。入力内容を確認してください。");
      body.put("error", ex.getMostSpecificCause().getMessage()); // より具体的な原因メッセージ
      body.put("timestamp", LocalDateTime.now().toString());
      return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  // 認証エラー (AuthenticationException.javaを基に)
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
      Map<String, Object> body = new HashMap<>();
      body.put("status", HttpStatus.UNAUTHORIZED.value()); // 401 Unauthorized
      body.put("message", ex.getMessage());
      body.put("error", "Authentication Failed");
      body.put("timestamp", LocalDateTime.now().toString());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  // 引数の型不一致 (例: パス変数やクエリパラメータの型が異なる場合)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
      Map<String, Object> body = new HashMap<>();
      body.put("status", HttpStatus.BAD_REQUEST.value());
      body.put("message", String.format("パラメータ '%s' の型が不正です。正しい形式で入力してください。", ex.getName()));
      body.put("error", "Type Mismatch");
      body.put("timestamp", LocalDateTime.now().toString());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // 必須パラメータ不足
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
      Map<String, Object> body = new HashMap<>();
      body.put("status", HttpStatus.BAD_REQUEST.value());
      body.put("message", String.format("必須パラメータ '%s' が不足しています。", ex.getParameterName()));
      body.put("error", "Missing Parameter");
      body.put("timestamp", LocalDateTime.now().toString());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // IllegalArgumentException (InventoryServiceで使用)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
    ex.printStackTrace(); // 開発中はログ出力推奨
      Map<String, Object> body = new HashMap<>();
      body.put("status", HttpStatus.BAD_REQUEST.value());
      body.put("message", ex.getMessage());
      body.put("error", "Invalid Argument");
      body.put("timestamp", LocalDateTime.now().toString());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  // 汎用的な予期せぬ例外
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    ex.printStackTrace(); // 開発中はログ出力推奨
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("message", "サーバー内部で予期せぬエラーが発生しました");
    body.put("error", ex.getClass().getSimpleName());
    body.put("timestamp", LocalDateTime.now().toString());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}